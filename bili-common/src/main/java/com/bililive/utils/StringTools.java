package com.bililive.utils;

public class StringTools {
    public static String getFileSuffix(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        //截取：从点的下一位到末尾（避免返回".mp4"，只返回"mp4"）
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * 将字符串的首字母大写
     * @param str 原始字符串
     * @return 首字母大写后的字符串
     */
    public static String upperCaseFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 将字符串的首字母小写
     * @param str 原始字符串
     * @return 首字母小写后的字符串
     */
    public static String lowerCaseFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

}
