[![Download](https://img.shields.io/badge/download-latestversion-blue.svg)](https://bintray.com/kingkingdu/maven/img-compressor/_latestVersion) [![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

[中文版](README-zh-rCN.md)


### ImgCompressPlugin

* * *
An Android gradle plugin that scans the resource directory in the project and performs batch image compression. It provides 3 compression methods to support lossy and lossless compression. It also records compressed files, which is suitable for personal and team collaboration.


### how to use
Step 1: in the `project`'s build.gradle file:
```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        ...
        classpath 'com.kingkingdu.plugin:img-compressor:版本号'
    }
}
...
...
//Image compressor configuration
apply plugin: 'img-compressor'
imgCompressOpt{
    way="pngquant"
    test = false
    whiteFiles=["text_pic1.png","test_pic2.jpg"]
    minSize=5
    tinyKeys=["your key"]
}

```

Step 2: After the introduction, click `sync now`. After the gradle configuration is completed, the following figure `imgCompressTask` will be displayed. Double-click it to execute, wait for the compression result.
![gradle_guide](imgsource/gradle_guide.png)

#### configuration info
- `way`:Set the compression method, support 3 common compressions,include "tinypng", "pngquant", "zopflip" ,3 select 1, compression method selection and compression effect see the following figure
- `test`:Set whether the test mode is on, `false` means that the compressed image directly overwrites the original image, and `true` means that the original image and the compressed image are output to the test directory(Project/ImageCompressTest).
- `whiteFiles`:Optional, whitelisted file array, which will no compression 
- `minSize`:The unit is KB,the original image is larger than a certain value to trigger compression, and 0 means all compression.
- `tinyKeys`:Optional, only needed in the case of way="tinypng"


### Compression effect

| origin pic | tinypng | pngquant | zopflip |
| --- | --- | --- | --- |
| Compression type | Lossy | Lossy | Lossless |
| 1.3M | 445KB | 542KB |903KB |
| ![原图](imgsource/test_pic8.png) | ![tiny](imgsource/test_pic8(tiny).png) | ![pngquant](imgsource/test_pic8(pngquant).png) | ![zopflip](imgsource/test_pic8(zopflip).png) |



### How to choose the right compression method
- If the original compression method used by the project itself is the same as one of the three modes, then way selects the same way.
- If there is no fixed compression method, then it is recommended to use tiny or pngquant. Relatively speaking, tiny compression use time is longer, you need to apply for the key on the official website, but the effect is best. pngquant compression is fast, the effect is not bad.

### The meaning of the white list
- When the image is compressed and found to have a small amount of image distortion, you can add a whitelist to avoid being compressed.
### test mode
- Suitable for the scene: UI designers may need to help compare the compressed image to the distortion, but he is no code permission for the project, so the compressed image is placed in a directory and packaged for review by the designer. Of course, the most convenient way is Version comparison tool that comes with Android studio.
- When `test=true` in the configuration, the original image and the compressed image are output to the test directory `(Project/ImageCompressTest)`, and the compressed image is named `xxxx(test).png`
- Note: When there is only the original image in the test directory but there is no compressed image, it means the image is fully compressed and no need to compress.

### Best Practices
- Introduce the plugin, configure the test mode test=false, after executing the `imgCompressTask` for the first time, check the log and print a similar log:
  - Task finish, compressed:3 files skip:3 Files before total size: 951.50KB after total size: 309.67KB save size: 641.83KB
  - The log shows that the compressed picture is 3, the skipped picture is 3, and the size of the picture before and after compression is compared. The reason for the picture skipping is that the picture is fully compressed and does not need to be compressed.

- After executing the task, the `image-compressed-info.json` file will be generated in the project directory, and the compressed image information will be recorded.
- Through Android Studio's own version control to compare whether the image before and after compression is distorted, add distortion pictures (usually rarely) to the white list, and revert to restore the original image
- Add `image-compressed-info.json` file to version control, submit modified image and json file
- In the subsequent version development, anyone on the team will add any images during the development process, or after the version is tested, a task can be executed to compress the images imported by the new version. The old version of the image remains unchanged. also will not be recompress by other team members.


### Answers
- Q1: If the compressed image is replaced with a new image during the subsequent iteration, will plugin forget to compress it?
A: No. The plugin itself will be verified by MD5, instead of the image naming to verify, so the new image will also perform compression
- Q2: What are the supported image formats?
A: support png, jpg format, webp temporarily does not support
- Q3: Is there a module for a componentized project, will all modules be scanned?
A: will be all scanned, as long as the application type or Android library type will be scanned

### Supported systems
Tested on macOS, windows10


### reference
[chenenyu](https://github.com/chenenyu/img-optimizer-gradle-plugin)
### License

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

