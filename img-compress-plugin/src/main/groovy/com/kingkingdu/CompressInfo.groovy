package com.kingkingdu

public class CompressInfo{

    int preSize;
    int compressedSize;
    String ratio;//压缩比例
    String path;
    String md5;

    CompressInfo(int preSize, int compressedSize, String path, String md5) {
        this.preSize = preSize
        this.compressedSize = compressedSize
        this.path = path
        this.md5 = md5

        ratio = compressedSize * 1.0F/preSize *100 +"%"
    }
}