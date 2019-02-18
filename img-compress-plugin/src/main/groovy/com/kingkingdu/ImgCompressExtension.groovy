package com.kingkingdu
/**
 * 压缩配置
 */
class ImgCompressExtension {

    String way = Constants.WAY_QUANT//压缩模式
    int minSize = 0 //单位kb,>minSize(kb)的图片才执行压缩
    ArrayList<String> whiteDirs = new ArrayList<String>()//白名单目录,不进行压缩,后续开发
    ArrayList<String> whiteFiles = new ArrayList<String>()//白名单文件,不进行压缩

    ArrayList<String> tinyKeys = new ArrayList<String>()//tiny模式下的秘钥
    boolean test = false //是否开启测试模式,会生成测试目录,方便比较图片压缩的前后效果

}