package com.kingkingdu.compressor

import com.kingkingdu.Constants

public class CompressorFactory {

    public static ICompressor getCompressor(String way){
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
        }
    }

}