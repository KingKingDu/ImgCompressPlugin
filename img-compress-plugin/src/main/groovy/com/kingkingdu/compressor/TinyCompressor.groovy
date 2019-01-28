package com.kingkingdu.compressor

import com.kingkingdu.CompressInfo
import com.kingkingdu.ImgCompressExtension
import com.kingkingdu.Utils
import groovy.json.JsonOutput
import org.gradle.api.Project
import com.tinify.*
public class TinyCompressor implements ICompressor{
    int keyIndex = 0
    def rootProject;
    def compressInfoList = new ArrayList<CompressInfo>()
    boolean accountError = false
    ImgCompressExtension config;
    def newCompressedList = new ArrayList<CompressInfo>()
    def beforeTotalSize = 0
    def afterTotalSize = 0
    List<String> sizeDirList = ["原图500KB以上","原图200KB以上","原图100KB以上","原图50KB以上","原图20KB以上","原图20KB以下"]


    @Override
    void compress(Project rootProject, List<File> files, List<CompressInfo> compressInfoList, ImgCompressExtension config) {
        this.rootProject = rootProject;
        this.compressInfoList = compressInfoList;
        this.config = config
        checkKey()

        files:for(File dir: files){
            dir : for (File file:dir.listFiles()){
                if (accountError) break files //跳出2层循环
                tryCompressSingleFile(file,dir)
            }
        }
        updateCompressInfoList()

        println("Tiny Finished beforeTotalSize:${beforeTotalSize}  afterTotalSize:${afterTotalSize}")

    }

    def tryCompressSingleFile(File file,File parentDir){
        def filePath = file.path
        def fileName = file.name
        def md5 = Utils.generateMD5(file)
        //过滤白名单文件
        if (!config.whiteFiles.empty){
            for (String s:config.whiteFiles){
                if (fileName.equals(s)){
                    return
                }
            }
        }

        //过滤已压缩文件
        for (CompressInfo info : compressInfoList){
            if (info.path.equals(fileName) && info.md5.equals(md5)){
                return
            }
        }

        if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
            //.9图剔除
            if (fileName.contains(".9")) {
                return
            }

            println("find target pic >>>>>>>>>>>>> $filePath")

            def fis = new FileInputStream(file)

            try {
                //available在读取之前知道数据流有多少个字节,即原始文件大小
                def beforeSize = fis.available()
                def beforeSizeStr = Utils.formetFileSize(beforeSize)

                // Use the Tinify API client
                def tSource = Tinify.fromFile("${parentDir}/${fileName}")
                if (config.test){
                    saveToTestDir(tSource,beforeSize,"${parentDir}",fileName)
                }else {
                    tSource.toFile("${parentDir}/${fileName}")
                }



                //这里没对压缩后如果文件变大做处理
                def afterSize = fis.available()
                def afterSizeStr = Utils.formetFileSize(afterSize)

                beforeTotalSize += beforeSize
                afterTotalSize += afterSize

                def info = new CompressInfo(beforeSize,afterSize,filePath,md5)
                newCompressedList.add(info)

                println("beforeSize: $beforeSizeStr -> afterSize: ${afterSizeStr} radio:${info.ratio}")
            } catch (AccountException e) {
                println("AccountException: ${e.getMessage()}")
                if (config.tinyKeys.size() <= ++keyIndex){
                    accountError = true
                    return
                }else {
                    //失败重试
                    Tinify.setKey(config.tinyKeys[keyIndex])
                    tryCompressSingleFile(file)
                }
                // Verify your API key and account limit.
            } catch (ClientException e) {
                // Check your source image and request options.
                println("ClientException: ${e.getMessage()}")
            } catch (ServerException e) {
                // Temporary issue with the Tinify API.
                println("ServerException: ${e.getMessage()}")
            } catch (ConnectionException e) {
                // A network connection error occurred.
                println("ConnectionException: ${e.getMessage()}")
            } catch (IOException e) {
                // Something else went wrong, unrelated to the Tinify API.
                println("IOException: ${e.getMessage()}")
            } catch (Exception e) {
                println("Exception: ${e.toString()}")
            }
        }

    }

    def checkKey(){
        if (config.tinyKeys.empty){
            println("Tiny tinyKeys is empty.")
            return
        }
        //测试key值的正确性
        Tinify.setKey("${config.tinyKeys[keyIndex]}")
        Tinify.validate()

    }

    def updateCompressInfoList(){
        for (CompressInfo newTinyPng : newCompressedList) {
            def index = compressInfoList.path.indexOf(newTinyPng.path)
            if (index >= 0) {
                compressInfoList[index] = newTinyPng
            } else {
                compressInfoList.add(0, newTinyPng)
            }
        }
        def jsonOutput = new JsonOutput()
        def json = jsonOutput.toJson(compressInfoList)


        def compressedListFile = new File("${rootProject.projectDir}/imageCompressedInfo.json")
        if (!compressedListFile.exists()) {
            compressedListFile.createNewFile()
        }
        compressedListFile.write(jsonOutput.prettyPrint(json), "utf-8")
        println("Task finish, compress ${newCompressedList.size()} files, before total size: ${Utils.formetFileSize(beforeSize)} after total size: ${Utils.formetFileSize(afterSize)}")
    }

    def saveToTestDir(Source source,int beforeSize,String originPath,String originName){
        def testDir = new File("${rootProject.projectDir}/ImageCompressTest")
        if (!testDir.exists()) {
            testDir.mkdir()
            for(String sizeDir:sizeDirList){
                def sizePath = new File("${rootProject.projectDir}/ImageCompressTest/${sizeDir}")
                if (!sizePath.exists()) sizePath.mkdir()
            }
        }

        def imageFile ="";
        if (beforeSize < 1024 * 20) {
            imageFile =  "${rootProject.projectDir}/ImageCompressTest/${sizeDirList[5]}"
        }else if (beforeSize < 1024 * 50){
            imageFile =  "${rootProject.projectDir}/ImageCompressTest/${sizeDirList[4]}"
        }else if (beforeSize < 1024 * 100){
            imageFile =  "${rootProject.projectDir}/ImageCompressTest/${sizeDirList[3]}"
        }else if (beforeSize < 1024 * 200){
            imageFile =  "${rootProject.projectDir}/ImageCompressTest/${sizeDirList[2]}"
        }else if (beforeSize < 1024 * 500){
            imageFile =  "${rootProject.projectDir}/ImageCompressTest/${sizeDirList[1]}"
        }else {
            imageFile =  "${rootProject.projectDir}/ImageCompressTest/${sizeDirList[0]}"
        }

        def originFile = new File("${originPath}/${originName}")
        def copyFile = new File("${imageFile}/${originName}")
        copyFile << originFile.text

        source.toFile("${imageFile}/${originName}"+"_test")



    }

}