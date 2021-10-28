package com.kingkingdu

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.Plugin

class ImgCompressPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {
        println("ImgCompressPlugin  call " + project.name + "  gradle:" + project.gradle.toString()  +" " +(project == project.getRootProject()))
        if (!project == project.getRootProject()){
            throw new IllegalArgumentException("img-compress-plugin must works on project level gradle")
        }
        project.extensions.create(Constants.EXT_OPT,ImgCompressExtension)
        project.tasks.create(Constants.TASK_NAME,ImgCompressTask) {
            it.group = "image"
            it.description = "Compress  images"
        }
    }
}
