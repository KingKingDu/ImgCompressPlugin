package com.kingkingdu.compressor

import com.kingkingdu.CompressInfo
import com.kingkingdu.ImgCompressExtension
import com.kingkingdu.ResultInfo
import com.kingkingdu.util.FileUtils
import com.kingkingdu.util.Logger
import com.kingkingdu.util.PngquantUtil
import com.kingkingdu.util.ZopflipngUtil
import org.gradle.api.Project

public class ZopflipngCompressor implements ICompressor{
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
        ZopflipngUtil.copyZopflipng2BuildFolder(project)
        log.i("type>>ZopflipngCompressor init....")
        PngquantUtil.copyPngquant2BuildFolder(project)
        def zopflipng = ZopflipngUtil.getZopflipngFilePath(project)
        unCompressFileList.each { info ->
            File originFile = new File(info.path)
            String type = originFile.getAbsolutePath().substring(originFile.getAbsolutePath().indexOf("."))
            long originalSize = originFile.length()
            Process process = new ProcessBuilder(zopflipng, "-y", "-m",info.path,info.outputPath).redirectErrorStream(true).start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))
            StringBuilder error = new StringBuilder()
            String line
            while (null != (line = br.readLine())) {
                error.append(line)
            }
            int exitCode = process.waitFor()

            if (exitCode == 0) {
                long optimizedSize = new File(info.outputPath).length()
                float rate = 1.0f * (originalSize - optimizedSize) / originalSize * 100
                info.update(originalSize,optimizedSize,FileUtils.generateMD5(new File(info.outputPath)))
                log.i("Succeed! ${FileUtils.formetFileSize(originalSize)}-->${FileUtils.formetFileSize(optimizedSize)}, ${rate}% saved! ${info.outputPath}")
                beforeTotalSize += originalSize
                afterTotalSize += optimizedSize
            } else if (exitCode == 98) {
                log.w("Skipped! ${info.outputPath}")
                skipCount++
            } else {
                log.e("Failed! ${info.outputPath}")
                skipCount++
            }
        }

//        log.i("Task finish, compress ${unCompressFileList.size()} files, before total size: ${FileUtils.formetFileSize(beforeTotalSize)} after total size: ${FileUtils.formetFileSize(afterTotalSize)}")
        resultInfo.compressedSize = unCompressFileList.size()
        resultInfo.beforeSize = beforeTotalSize
        resultInfo.afterSize = afterTotalSize
        resultInfo.skipCount = skipCount
    }






}