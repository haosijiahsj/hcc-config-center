package com.hcc.config.center.client.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Json
 *
 * @author shengjun.hu
 * @date 2022/9/16
 */
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    private JsonUtils() {}

    /**
     * 读Json
     *
     * @param jsonString 输入
     * @param tClass     适配的类
     * @param <T>
     * @return
     */
    public static <T> T toObject(String jsonString, Class<T> tClass) {
        try {
            return OBJECT_MAPPER.readValue(jsonString, tClass);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public static <T> Set<T> toSet(String jsonString, Class<T> elementClass) {
        try {
            CollectionType listType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(HashSet.class, elementClass);
            Set<T> set = OBJECT_MAPPER.readValue(jsonString, listType);
            return set;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }


    /**
     * 读Json,数组可以用这个
     *
     * @param jsonString 输入
     * @param <T>
     * @return
     */
    public static <T> T toObject(String jsonString, TypeReference<T> valueTypeRef) {
        try {
            return OBJECT_MAPPER.readValue(jsonString, valueTypeRef);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }


    public static <T> List<T> toList(String jsonString, Class<T> elementClass) {
        try {
            CollectionType listType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, elementClass);
            List<T> list = OBJECT_MAPPER.readValue(jsonString, listType);
            return list;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * 写String
     *
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
