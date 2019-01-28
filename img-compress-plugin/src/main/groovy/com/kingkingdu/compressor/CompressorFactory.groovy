package com.kingkingdu.compressor

import com.kingkingdu.Constants

public class CompressorFactory {

    public static ICompressor getCompressor(String way){
        switch (way){
            case Constants.WAY_TINY:
                return new TinyCompressor()
                break;
            case Constants.WAY_QUANT:
                return new QuantCompressor()
                break;
            case Constants.WAY_ZOPFLIP:
                return new ZopflipCompressor()
                break;
        }
    }

}