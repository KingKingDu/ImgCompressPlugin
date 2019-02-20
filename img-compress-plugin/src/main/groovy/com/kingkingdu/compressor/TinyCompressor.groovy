package com.kingkingdu.compressor

import com.kingkingdu.CompressInfo
import com.kingkingdu.ImgCompressExtension
import com.kingkingdu.ResultInfo
import com.kingkingdu.util.FileUtils
import com.kingkingdu.util.Logger
import org.gradle.api.Project
import com.tinify.*
public class TinyCompressor implements ICompressor{
    int keyIndex = 0
    def rootProject;
    def compressInfoList = new ArrayList<CompressInfo>()
    boolean accountError = false
    ImgCompressExtension config;
    def beforeTotalSize = 0
    def afterTotalSize = 0
    Logger log

    @Override
    void compress(Project rootProject, List<CompressInfo> unCompressFileList, ImgCompressExtension config, ResultInfo resultInfo) {
        this.rootProject = rootProject;
        this.compressInfoList = compressInfoList;
        this.config = config
        log = Logger.getInstance(rootProject)
        checkKey()
        unCompressFileList.each {
            tryCompressSingleFile(it)
        }
//        println("Task finish, compress ${unCompressFileList.size()} files, before total size: ${FileUtils.formetFileSize(beforeTotalSize)} after total size: ${FileUtils.formetFileSize(afterTotalSize)}")
        resultInfo.compressedSize = unCompressFileList.size()
        resultInfo.beforeSize = beforeTotalSize
        resultInfo.afterSize = afterTotalSize
        resultInfo.skipCount = 0

    }

    def tryCompressSingleFile(CompressInfo info){

        println("find target pic >>>>>>>>>>>>> ${info.path}")
        try {
            def fis = new FileInputStream(new File(info.path))
            //available在读取之前知道数据流有多少个字节,即原始文件大小
            def beforeSize = fis.available()
            def beforeSizeStr = FileUtils.formetFileSize(beforeSize)

            // Use the Tinify API client
            def tSource = Tinify.fromFile(info.path)
            tSource.toFile(info.outputPath)


            fis = new FileInputStream(new File(info.outputPath))
            //这里没对压缩后如果文件变大做处理
            def afterSize = fis.available()
            def afterSizeStr = FileUtils.formetFileSize(afterSize)

            beforeTotalSize += beforeSize
            afterTotalSize += afterSize
            info.update(beforeSize,afterSize,FileUtils.generateMD5(new File(info.outputPath)))
            log.i("beforeSize: $beforeSizeStr -> afterSize: ${afterSizeStr} radio:${info.ratio}")
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

    def checkKey(){
        if (config.tinyKeys.empty){
            println("Tiny tinyKeys is empty.")
            return
        }
        try {
            //测试key值的正确性
            Tinify.setKey("${config.tinyKeys[keyIndex]}")
            Tinify.validate()
        } catch (AccountException ex) {
            println("TinyCompressor" + ex.printStackTrace())
        }


    }




}