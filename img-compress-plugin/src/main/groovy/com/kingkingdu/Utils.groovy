package com.kingkingdu

import java.security.MessageDigest
import java.text.DecimalFormat

public class Utils {

    public static String generateMD5(File file) {
        MessageDigest digest = MessageDigest.getInstance("MD5")
        file.withInputStream(){ is ->
            int read
            byte[] buffer = new byte[8192]
            while((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] md5sum = digest.digest()
        BigInteger bigInt = new BigInteger(1, md5sum)
        return bigInt.toString(16).padLeft(32, '0')
    }

    public static String formetFileSize(long fileS) {
        def df = new DecimalFormat("#.00")
        if (fileS == 0L) {
            return "0B"
        }

        if (fileS < 1024) {
            return df.format((double) fileS) + "B"
        } else if (fileS < 1048576) {
            return df.format((double) fileS / 1024) + "KB"
        } else if (fileS < 1073741824) {
            return df.format((double) fileS / 1048576) + "MB"
        } else {
            return df.format((double) fileS / 1073741824) + "GB"
        }
    }

}