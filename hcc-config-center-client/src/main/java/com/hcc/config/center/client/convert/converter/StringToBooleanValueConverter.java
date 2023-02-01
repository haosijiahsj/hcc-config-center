package com.hcc.config.center.client.convert.converter;

import com.hcc.config.center.client.convert.ValueConverter;

import java.util.HashSet;
import java.util.Set;

/**
 * String -> Boolean
 *
 * @author shengjun.hu
 * @date 2022/11/2
 */
public class StringToBooleanValueConverter implements ValueConverter<Boolean> {

    private static final Set<String> trueValues = new HashSet<>(4);
    private static final Set<String> falseValues = new HashSet<>(4);

    static {
        trueValues.add("true");
        trueValues.add("on");
        trueValues.add("yes");
        trueValues.add("1");

        falseValues.add("false");
        falseValues.add("off");
        falseValues.add("no");
        falseValues.add("0");
    }

    @Override
    public Boolean convert(String value, Class targetClass) {
        String tmpValue = value.toLowerCase();
        if (trueValues.contains(tmpValue)) {
            return Boolean.TRUE;
        }
        else if (falseValues.contains(tmpValue)) {
            return Boolean.FALSE;
        }

        throw new IllegalArgumentException(String.format("非法的Boolean值：[%s]", value));
    }

}
