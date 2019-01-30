package com.kingkingdu.compressor

import com.kingkingdu.Constants
import com.kingkingdu.util.Logger
import org.gradle.api.Project

public class CompressorFactory {

    public static ICompressor getCompressor(String way, Project project){
        switch (way){
            case Constants.WAY_TINY:
                return new TinyCompressor()
                break;
            case Constants.WAY_QUANT:
                return new PngquantCompressor()
                break;
            case Constants.WAY_ZOPFLIP:
                return new ZopflipngCompressor()
                break;
            default:
                String errorMsg = "imgCompressOpt field 'way' error ,way must setting with one of [${Constants.WAY_TINY} ,${Constants.WAY_QUANT},${Constants.WAY_ZOPFLIP}]"
                Logger.getInstance(project.rootProject).e(errorMsg)
                throw new IllegalArgumentException(errorMsg)
                break
        }
    }

}