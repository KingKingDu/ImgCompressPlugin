package com.kingkingdu
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kingkingdu.CompressInfo
import com.kingkingdu.ImgCompressExtension
import com.kingkingdu.compressor.CompressorFactory
import com.kingkingdu.util.FileUtils
import com.kingkingdu.util.Logger
import com.sun.org.apache.bcel.internal.generic.NEW
import com.tinify.Source
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import jdk.nashorn.internal.ir.WhileNode
import org.gradle.api.DefaultTask
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files

public class ImgCompressTask extends DefaultTask {
    ImgCompressExtension config
    Logger log
    List<String> sizeDirList = [">500KB", "200~500KB", "100~200KB", "50~100KB", "20~50KB", "<20KB"]

    ImgCompressTask() {
        description = 'ImgCompressTask'
        group = 'imgCompress'
        config = project.imgCompressOpt
    }


    @TaskAction
    def run() {
        log = Logger.getInstance(project.getProject())
        log.i("ImgCompressTask run")

        if (!project == project.getProject()) {
            throw new IllegalArgumentException("img-compress-plugin must works on project level gradle")
        }
        def imgDirectories = getSourcesDirs(project)
        def compressedList = getCompressedInfo()
        def unCompressFileList = getUnCompressFileList(imgDirectories, compressedList)

        CompressorFactory.getCompressor(config.way, project).compress(project, unCompressFileList, config)
        copyToTestPath(unCompressFileList)
        updateCompressInfoList(unCompressFileList, compressedList)
    }

    /**
     * 获取所有的资源目录
     * @param root
     * @return
     */
    List<File> getSourcesDirs(Project root) {
        List<File> dirs = []
        root.allprojects {
            project ->
                //仅对两种module做处理
                log.i("ImgCompressTask deal ${project.name}")
                if (project.plugins.hasPlugin(AppPlugin)) {
                    dirs.addAll(getSourcesDirsWithVariant((DomainObjectCollection<BaseVariant>) project.android.applicationVariants))
                } else if (project.plugins.hasPlugin(LibraryPlugin)) {
                    dirs.addAll(getSourcesDirsWithVariant((DomainObjectCollection<BaseVariant>) project.android.libraryVariants))
                } else {
                    log.i("ignore project:" + project.name)
                }
        }
        log.i("dirs size = ${dirs.size()}")
        return dirs
    }
    /**
     * 根据当前module的variant获取所有打包方式的资源目录
     * @param collection
     * @return
     */
    List<File> getSourcesDirsWithVariant(DomainObjectCollection<BaseVariant> collection) {
        List<File> imgDirectories = []
        collection.all { variant ->
            log.i("-------- variant: $variant.name --------")
            variant.sourceSets?.each { sourceSet ->
                log.i("sourceSets.${sourceSet.name} -->")
                if (sourceSet.resDirectories.empty) return
                sourceSet.resDirectories.each { res ->
                    if (res.exists()) {
                        log.i("${res.name}.directories:")
                        if (res.listFiles() == null) return
                        res.eachDir {
                            //收集所有drawable目录及mipmap目录
                            if (it.directory && (it.name.startsWith("drawable") || it.name.startsWith("mipmap"))) {
                                if (!imgDirectories.contains(it)) {
                                    log.i("add dir $it")
                                    imgDirectories << it
                                }
                            }
                        }
                    }
                }
            }

        }
        return imgDirectories
    }

    /**
     * 获取之前压缩文件信息
     * @return
     */
    List<CompressInfo> getCompressedInfo() {
        //读取原先已压缩过的文件,如果压缩过则不再压缩
        def compressedList = new ArrayList<CompressInfo>()
        def compressedListFile = new File("${project.projectDir}/imageCompressedInfo.json")
        if (!compressedListFile.exists()) {
            compressedListFile.createNewFile()
        } else {
            try {
                //将已压缩过的文件json解析-->list

                def json = new FileInputStream(compressedListFile).getText("utf-8")
                def list = new Gson().fromJson(json, new TypeToken<ArrayList<CompressInfo>>() {
                }.getType())
                if (list instanceof ArrayList) {
                    compressedList = list
                } else {
                    log.i("compressed-resource.json is invalid, ignore")
                }
            } catch (Exception ignored) {
                log.i("compressed-resource.json is invalid, ignore")
            }
        }

        log.i("getCompressedInfo size=${compressedList.size()}")
        return compressedList


    }

    /**
     * 获取待压缩的文件,过滤白名单目录及文件
     * @param imgDirectories
     * @param compressedList
     * @return
     */
    List<CompressInfo> getUnCompressFileList(List<File> imgDirectories, List<CompressInfo> compressedList) {
        List<CompressInfo> unCompressFileList = new ArrayList<>()

        dirFlag:
        for (File dir : imgDirectories) {
            //剔除白名单目录
//            if (!config.whiteDirs.empty) {
//                for (String whiteDir : config.whiteDirs) {
//                    if (whiteDir.equals(dir.getAbsolutePath())) {
//                        log.i("ignore whiteDirectory >> " + directory.getAbsolutePath())
//                        continue dirFlag
//                    }
//                }
//            }

            fileFlag:
            for (File it : dir.listFiles()) {
                String fileName = it.name
                log.i("fileName ${fileName}")

                //过滤白名单文件
                if (!config.whiteFiles.empty) {
                    for (String s : config.whiteFiles) {
                        if (fileName.equals(s)) {
                            log.i("ignore whiteFiles >> " + it.getAbsolutePath())
                            continue fileFlag
                        }
                    }
                }
                def newMd5 = FileUtils.generateMD5(it)
                //过滤已压缩文件
                for (CompressInfo info : compressedList) {
                    log.i("origin : $newMd5   info.md5:${info.md5}  + ${info.md5.equals(newMd5)}")
                    //md5校验
                    if (info.md5.equals(newMd5)) {
                        log.i("ignore compressed >> " + it.getAbsolutePath())
                        continue fileFlag
                    }
                }
                if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
                    //.9图剔除
                    if (fileName.contains(".9")) {
                        log.i("ignore 9.png >> " + it.getAbsolutePath())
                        continue fileFlag
                    }
                    unCompressFileList.add(new CompressInfo(-1, -1, "", it.getAbsolutePath(), getOutputPath(it), newMd5))
                    log.i("add file   >> " + it.getAbsolutePath())
                    log.i("outputPath >> " + getOutputPath(it))

                }

            }


        }

        return unCompressFileList


    }

    /**
     * 根据测试配置确定输出路径
     * @param originImg
     * @return
     */
    String getOutputPath(File originImg) {

        if (config.test) {
            def testDir = new File("${project.projectDir}/ImageCompressTest")
            if (!testDir.exists()) {
                testDir.mkdir()
                for (String sizeDir : sizeDirList) {
                    def sizePath = new File("${project.projectDir}/ImageCompressTest/${sizeDir}")
                    if (!sizePath.exists()) sizePath.mkdir()
                }
            }
            def outPutPath = originImg.absolutePath
            def fis = new FileInputStream(originImg)
            def beforeSize = originImg == null ? 0 : fis.available()
            def originName = originImg.getName()
            def typeIndex = originName.indexOf(".")
            def testName = originName.substring(0,typeIndex) +"(test)" + originName.substring(typeIndex,originName.length())
            log.i("testName >> " + testName)
            if (beforeSize < 1024 * 20) {
                outPutPath = "${project.projectDir}/ImageCompressTest/${sizeDirList[5]}/${testName}"
            } else if (beforeSize < 1024 * 50) {
                outPutPath = "${project.projectDir}/ImageCompressTest/${sizeDirList[4]}/${testName}"
            } else if (beforeSize < 1024 * 100) {
                outPutPath = "${project.projectDir}/ImageCompressTest/${sizeDirList[3]}/${testName}"
            } else if (beforeSize < 1024 * 200) {
                outPutPath = "${project.projectDir}/ImageCompressTest/${sizeDirList[2]}/${testName}"
            } else if (beforeSize < 1024 * 500) {
                outPutPath = "${project.projectDir}/ImageCompressTest/${sizeDirList[1]}/${testName}"
            } else {
                outPutPath = "${project.projectDir}/ImageCompressTest/${sizeDirList[0]}/${testName}"
            }
            return outPutPath

        } else {
            return originImg.getAbsolutePath()
        }

    }

    /**
     * 更新已压缩信息
     * @param newCompressedList
     * @param compressedList
     * @return
     */
    def updateCompressInfoList(List<CompressInfo> newCompressedList, List<CompressInfo> compressedList) {
        //脱敏
        String projectDir = project.projectDir.getAbsolutePath()
        for (CompressInfo info:newCompressedList){
            info.path = info.path.substring(projectDir.length(),info.path.length())
            info.outputPath = info.outputPath.substring(projectDir.length(),info.outputPath.length())
            println("updateCompressInfoList >> ${info.path}")
            println("updateCompressInfoList >> ${info.outputPath}")
        }
        for (CompressInfo newTinyPng : newCompressedList) {
            def index = compressedList.md5.indexOf(newTinyPng.md5)
            if (index >= 0) {
                compressedList[index] = newTinyPng
            } else {
                compressedList.add(0, newTinyPng)
            }
        }
        def jsonOutput = new JsonOutput()
        def json = jsonOutput.toJson(compressedList)

        def compressedListFile = new File("${project.projectDir}/imageCompressedInfo.json")
        if (!compressedListFile.exists()) {
            compressedListFile.createNewFile()
        }
        compressedListFile.write(jsonOutput.prettyPrint(json), "utf-8")
    }

    /**
     * 复制原文件到测试目录,便于比对
     * @param newCompressedList
     * @return
     */
    def copyToTestPath(List<CompressInfo> newCompressedList){
        newCompressedList.each  { info ->
            File origin = new File(info.path)
            String testPathName = new File(info.outputPath).parent +"/" + origin.getName()
            File copyFile = new File(testPathName)
            if (copyFile.exists()){
                copyFile.delete()
            }
            log.i("copyToTestPath >>" + testPathName)
            try {
                Files.copy(origin.toPath(),copyFile.toPath())
            } catch (Exception e){
                log.i("copyToTestPath" + e.printStackTrace())
            }

        }


    }

}