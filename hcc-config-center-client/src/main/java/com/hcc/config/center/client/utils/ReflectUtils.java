package com.hcc.config.center.client.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射工具类
 *
 * @author shengjun.hu
 * @date 2022/10/9
 */
public class ReflectUtils {

    /**
     * 反射设置值
     * @param obj
     * @param field
     * @param value
     * @throws IllegalAccessException
     */
    public static void setValue(Object obj, Field field, Object value) throws IllegalAccessException {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        field.set(obj, value);
        field.setAccessible(field.isAccessible());
    }

    /**
     * 反射调用方法
     * @param obj
     * @param method
     * @param value
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void invokeMethod(Object obj, Method method, Object...value) throws InvocationTargetException, IllegalAccessException {
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        method.invoke(obj, value);
        method.setAccessible(method.isAccessible());
    }

    /**
     * 是否存在某个注解
     * @param clazz
     * @param annotationClass
     * @return
     */
    public static boolean hasAnnotation(Class clazz, Class annotationClass) {
        return clazz.getAnnotation(annotationClass) != null;
    }

}
