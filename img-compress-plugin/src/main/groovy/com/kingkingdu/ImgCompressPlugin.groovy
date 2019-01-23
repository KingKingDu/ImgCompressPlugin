package com.kingkingdu

import org.gradle.api.Project
import org.gradle.api.Plugin

class ImgCompressPlugin implements Plugin<Project>{
    @Override
    void apply(Project project) {
        println("ImgCompressPlugin  call")
    }
}
