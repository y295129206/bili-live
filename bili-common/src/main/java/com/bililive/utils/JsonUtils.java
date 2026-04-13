package com.bililive.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * JSON 工具类
 * 提供 JSON 与 Java 对象之间的转换方法
 */
public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    /**
     * 将 Java 对象转换为 JSON 字符串
     *
     * @param obj Java 对象
     * @return JSON 字符串
     */
    public static String convertObj2Json(Object obj) {
        if (obj == null) {
            return null;
        }
        return JSON.toJSONString(obj);
    }

    /**
     * 将 JSON 字符串转换为 Java 对象
     *
     * @param json   JSON 字符串
     * @param classz 目标类型
     * @param <T>    泛型
     * @return Java 对象
     */
    public static <T> T convertJson2Obj(String json, Class<T> classz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return JSONObject.parseObject(json, classz);
        } catch (Exception e) {
            logger.error("JSON 转对象失败：json={}, class={}", json, classz.getName(), e);
            return null;
        }
    }

    /**
     * 将 JSON 数组字符串转换为 List
     *
     * @param json   JSON 数组字符串
     * @param classz 元素类型
     * @param <T>    泛型
     * @return List 对象
     */
    public static <T> List<T> convertJsonArray2List(String json, Class<T> classz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return JSONArray.parseArray(json, classz);
        } catch (Exception e) {
            logger.error("JSON 数组转 List 失败：json={}, class={}", json, classz.getName(), e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串转换为 JSONObject
     *
     * @param json JSON 字符串
     * @return JSONObject
     */
    public static JSONObject parseObject(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return JSONObject.parseObject(json);
        } catch (Exception e) {
            logger.error("解析 JSON 对象失败：json={}", json, e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串转换为 JSONArray
     *
     * @param json JSON 字符串
     * @return JSONArray
     */
    public static JSONArray parseArray(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return JSONArray.parseArray(json);
        } catch (Exception e) {
            logger.error("解析 JSON 数组失败：json={}", json, e);
            return null;
        }
    }
}
