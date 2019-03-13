package com.kingkingdu.util

import org.gradle.api.Project

import java.text.SimpleDateFormat

/**
 * @Author: chenenyu
 * @Created: 16/6/30 15:21.
 */
class Logger {

    private static final String LOG_FILE_NAME = "img_compress.log";
    private static final String INFO = "info:  ";
    private static final String WARN = "warn:  ";
    private static final String ERROR = "error: ";
    private static Logger  log;
    private File file;
    private Writer writer;
    private outPutLog = false

    Logger(Project project) {
        if (outPutLog){
            file = new File(project.projectDir.absolutePath + File.separator + LOG_FILE_NAME)
            new PrintWriter(file).close()
        }
    }

    public static Logger getInstance(Project project){
        if (log == null){
            synchronized (Logger.class){
                if (log == null){
                    log = new Logger(project)
                }
            }
        }

        return log
    }

    private def write(String logLevel, String msg) {
        if (!outPutLog) return
        writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, true), "UTF-8")), true)
        try {
            writer.write(getDateTime() + "  " + logLevel)
            writer.write(msg + "\r\n")
            writer.write("----------------------------------------\r\n")
        } catch (Exception e) {
        } finally {
            writer.close();
        }
    }

    private def getDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date())
    }

    def i(String msg) {
        write(INFO, msg)
        println(msg)
    }

    def w(String msg) {
        write(WARN, msg)
        println(msg)
    }

    def e(String msg) {
        write(ERROR, msg)
        println(msg)
    }

}