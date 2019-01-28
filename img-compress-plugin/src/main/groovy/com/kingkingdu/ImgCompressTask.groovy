package com.kingkingdu
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.kingkingdu.CompressInfo
import com.kingkingdu.ImgCompressExtension
import com.kingkingdu.compressor.CompressorFactory
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

public class ImgCompressTask extends DefaultTask{
    def android
    ImgCompressExtension config
    ImgCompressTask(){
        description = 'ImgCompressTask'
        group = 'imgCompress'
        config = project.imgCompressOpt
    }


    @TaskAction
    def run(){
        println("ImgCompressTask run")

        if (!project == project.getRootProject()){
            throw new IllegalArgumentException("img-compress-plugin must works on project level gradle")
        }
        def imgDirectories = getSourcesDirs(project)
        def compressedList = getCompressedInfo()
        CompressorFactory.getCompressor(config.way).compress(project,imgDirectories,compressedList,config)
    }


    List<File> getSourcesDirs(Project root){
        List<File> dirs = []
        root.allprojects{
            project ->
                //仅对两种module做处理
                println("ImgCompressTask deal ${project.name}")
                if (project.plugins.hasPlugin(AppPlugin)) {
                    dirs.addAll(getSourcesDirsWithVariant((DomainObjectCollection<BaseVariant>)project.android.applicationVariants))
                }else if(project.plugins.hasPlugin(LibraryPlugin)){
                    dirs.addAll(getSourcesDirsWithVariant((DomainObjectCollection<BaseVariant>)project.android.libraryVariants))
                }else {
                    println("ignore project:" + project.name)
                }
        }
        println("dirs size = ${dirs.size()}")
        return dirs
    }

    List<File> getSourcesDirsWithVariant(DomainObjectCollection<BaseVariant> collection){
        List<File> imgDirectories = []
        collection.all { variant ->
             println("-------- variant: $variant.name --------")
            variant.sourceSets?.each { sourceSet ->
                println("sourceSets.${sourceSet.name} -->")
                if (sourceSet.resDirectories.empty) return
                sourceSet.resDirectories.each { res ->
                    if (res.exists()) {
                        println("test.directories:" + (res.name == null))
                        println("${res.name}.directories:")
                        if (res.listFiles() == null) return
                        res.eachDir {
                            //收集所有drawable目录及mipmap目录
                            if (it.directory && (it.name.startsWith("drawable") || it.name.startsWith("mipmap"))) {
                                if (!config.whiteDirs.empty){
                                    config.whiteDirs.each{ whiteDir ->
                                        //剔除白名单目录
                                        if (!whiteDir.equals(it)){
                                            println("add dir $it")
                                            imgDirectories << it
                                        }
                                    }
                                }else {
                                    println("add dir $it")
                                    imgDirectories << it
                                }
                                // println("$it.absolutePath")
                            }
                        }
                    }
                }
            }

        }
        return imgDirectories
    }

    List<CompressInfo> getCompressedInfo(){
        //读取原先已压缩过的文件,如果压缩过则不再压缩
        def compressedList = new ArrayList<CompressInfo>()
        def compressedListFile = new File("${project.projectDir}/imageCompressedInfo.json")
        if (!compressedListFile.exists()) {
            compressedListFile.createNewFile()
        }
        else {
            try {
                //将已压缩过的文件json解析-->list
                def list = new JsonSlurper().parse(compressedListFile, "utf-8")
                if(list instanceof ArrayList) {
                    compressedList = list
                }
                else {
                    println("compressed-resource.json is invalid, ignore")
                }
            } catch (Exception ignored) {
                println("compressed-resource.json is invalid, ignore")
            }
        }

        println("getCompressedInfo size=${compressedList.size()}")
        return compressedList


    }
}