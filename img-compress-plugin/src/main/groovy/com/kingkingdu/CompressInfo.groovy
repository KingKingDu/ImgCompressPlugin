package com.kingkingdu

public class CompressInfo{

    int preSize;
    int compressedSize;
    String ratio;//压缩比例
    String path;
    String outputPath //输出目录
    String md5;

    CompressInfo(String path, String outputPath) {
        this.path = path
        this.outputPath = outputPath
    }
}