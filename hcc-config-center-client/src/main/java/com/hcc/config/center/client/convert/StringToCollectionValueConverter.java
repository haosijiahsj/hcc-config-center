package com.hcc.config.center.client.convert;

import com.hcc.config.center.client.utils.JsonUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * String -> Collection
 *
 * @author hushengjun
 * @date 2022/11/24
 */
public class StringToCollectionValueConverter implements ValueConverter<Collection> {

    private static final List<Class<?>> supportCollections = Arrays.asList(Collection.class, List.class, Set.class);

    private final Class<?>[] genericClasses;

    public StringToCollectionValueConverter(Class<?>[] genericClasses) {
        this.genericClasses = genericClasses;
    }

    @Override
    public Collection convert(String value, Class<? extends Collection> targetClass) {
        if (genericClasses == null || genericClasses.length == 0) {
            throw new IllegalArgumentException("未指定泛型实际类型");
        }
        if (!supportCollections.contains(targetClass)) {
            throw new IllegalArgumentException(String.format("不支持的集合类型：[%s]，仅支持Collection, List, Set", targetClass));
        }

        Collection<?> results;
        try {
            List<?> tempResults = JsonUtils.toList(value, genericClasses[0]);
            results = tempResults;
            if (Set.class.equals(targetClass)) {
                results = new HashSet(tempResults);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("无法将值：[%s]转换到目标类型：[%s]", value, targetClass), e);
        }

        return results;
    }

}
