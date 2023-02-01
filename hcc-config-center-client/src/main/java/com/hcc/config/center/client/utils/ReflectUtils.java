package com.hcc.config.center.client.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        field.set(obj, value);
        field.setAccessible(accessible);
    }

    /**
     * 反射调用方法
     * @param obj
     * @param method
     * @param args
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void invokeMethod(Object obj, Method method, Object...args) throws InvocationTargetException, IllegalAccessException {
        invokeMethod(obj, method, Object.class, args);
    }

    /**
     * 反射调用方法
     * @param obj
     * @param method
     * @param returnClass
     * @param args
     * @param <T>
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static <T> T invokeMethod(Object obj, Method method, Class<T> returnClass, Object... args) throws InvocationTargetException, IllegalAccessException {
        boolean accessible = method.isAccessible();
        if (!accessible) {
            method.setAccessible(true);
        }
        Object returnObj = method.invoke(obj, args);
        method.setAccessible(accessible);

        return returnClass.cast(returnObj);
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

    /**
     * 从字段中获取注解
     * @param field
     * @param annotationClass
     * @param <T>
     * @return
     */
    public static <T extends Annotation> T getAnnotation(Field field, Class<T> annotationClass) {
        if (field == null || annotationClass == null) {
            return null;
        }
        return field.getAnnotation(annotationClass);
    }

    /**
     * 从方法中获取注解
     * @param method
     * @param annotationClass
     * @param <T>
     * @return
     */
    public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
        if (method == null || annotationClass == null) {
            return null;
        }
        return method.getAnnotation(annotationClass);
    }

    /**
     * 获取字段的真实泛型
     * @param field
     * @return
     */
    public static Class[] getGenericClasses(Field field) {
        Type genericType = field.getGenericType();
        List<Class<?>> classes = new ArrayList<>();
        if (genericType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            for (Type type : actualTypeArguments) {
                classes.add((Class) type);
            }
        }

        return classes.toArray(new Class[0]);
    }

}
