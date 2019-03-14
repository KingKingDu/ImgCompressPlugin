package com.kingkingdu.compressor

import com.kingkingdu.CompressInfo
import com.kingkingdu.ImgCompressExtension
import com.kingkingdu.ResultInfo
import com.kingkingdu.util.FileUtils
import com.kingkingdu.util.Logger
import com.kingkingdu.util.PngquantUtil
import org.gradle.api.Project

import java.nio.file.Files

class PngquantCompressor implements ICompressor{
    def project;
    def compressInfoList = new ArrayList<CompressInfo>()
    ImgCompressExtension config
    def beforeTotalSize = 0
    def afterTotalSize = 0
    Logger log
    def skipCount=0 //用于压缩后变大的情况

    @Override
    void compress(Project rootProject, List<CompressInfo> unCompressFileList, ImgCompressExtension config, ResultInfo resultInfo) {
        this.project = rootProject
        this.compressInfoList = compressInfoList
        this.config = config
        log = Logger.getInstance(rootProject)
        PngquantUtil.copyPngquant2BuildFolder(project)
        def pngquant = PngquantUtil.getPngquantFilePath(project)
        unCompressFileList.each { info ->
            File originFile = new File(info.path)
            String type = originFile.getAbsolutePath().substring(originFile.getAbsolutePath().indexOf("."))
            String suffix = config.test?"(test)":""
//            log.i("type>>" + type)
            if (type.equals(".png")){
                suffix = config.test?"(test).png":".png"
            }

            long originalSize = originFile.length()
            Process process = new ProcessBuilder(pngquant, "-v", "--force","--skip-if-larger",
            "--speed=1",  "--ext=${suffix}",info.path).redirectErrorStream(true).start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))
            StringBuilder error = new StringBuilder()
            String line
            while (null != (line = br.readLine())) {
                error.append(line)
            }
            int exitCode = process.waitFor()
//            log.i("exitCode  ${exitCode}")
            if (exitCode == 0) {
                if (config.test){
                    //复制test文件到测试目录
                    String testName = new File(info.outputPath).name
                    String testPath = new File(info.path).parent +"/" + testName
                    copyToTestPath(testPath,info.outputPath)
                    File testFile = new File(testPath)
                    if (testFile.exists()){
                        testFile.delete()
                    }
                }


                long optimizedSize = new File(info.outputPath).length()
                float rate = 1.0f * (originalSize - optimizedSize) / originalSize * 100
                info.update(originalSize,optimizedSize,FileUtils.generateMD5(new File(info.outputPath)))
                log.i("Succeed! ${FileUtils.formetFileSize(originalSize)}-->${FileUtils.formetFileSize(optimizedSize)}, ${rate}% saved! ${info.outputPath}")
                beforeTotalSize += originalSize
                afterTotalSize += optimizedSize
            } else if (exitCode == 98) {
                log.w("Skipped! ${info.path}")
                skipCount++
            } else {
                log.e("Failed! ${info.path}")
                skipCount++
            }
        }

//        log.i("Task finish, compress ${unCompressFileList.size() - skipCount} files, before total size: ${FileUtils.formetFileSize(beforeTotalSize)} after total size: ${FileUtils.formetFileSize(afterTotalSize)}")
        resultInfo.compressedSize = unCompressFileList.size()-skipCount
        resultInfo.beforeSize = beforeTotalSize
        resultInfo.afterSize = afterTotalSize
        resultInfo.skipCount = skipCount
    }



    /**
     * 复制原文件到测试目录,便于比对
     * @param
     * @return
     */
    def copyToTestPath(String orginTestPath,String outputPath){
//        log.i("copyToTestPath originTestPath:${orginTestPath}  outputPath:${outputPath}")
        File origin = new File(orginTestPath)
        File copyFile = new File(outputPath)
        if (copyFile.exists()){
            copyFile.delete()
        }
        try {
            Files.copy(origin.toPath(),copyFile.toPath())
        } catch (Exception e){
            log.i("copyToTestPath" + e.printStackTrace())
        }


    }



}