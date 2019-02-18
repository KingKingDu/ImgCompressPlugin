package com.kingkingdu.compressor

import com.kingkingdu.CompressInfo
import com.kingkingdu.ImgCompressExtension
import com.kingkingdu.ResultInfo
import org.gradle.api.Project

/**
 * 压缩处理器抽象接口,有多种类型
 */
interface ICompressor {

    void compress(Project rootProject, List<CompressInfo> unCompressFileList, ImgCompressExtension config, ResultInfo resultInfo)
}