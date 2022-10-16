package com.hcc.config.center.domain.result;

import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PageResult
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Data
public class PageResult<T> {

    private Long page;
    private Long size;
    private Long totalPage;
    private Long totalSize;
    private List<T> data;

    public <V> PageResult<V> convertToTarget(Class<V> targetClass) {
        return this.convertToTarget(t -> {
            V v;
            try {
                v = targetClass.newInstance();
                BeanUtils.copyProperties(t, v);
            } catch (Exception e) {
                throw new IllegalStateException("转换异常！", e);
            }
            return v;
        });
    }

    public <V> PageResult<V> convertToTarget(ConvertAction<V, T> action) {
        PageResult<V> pageResult = new PageResult<>();
        BeanUtils.copyProperties(this, pageResult, "data");

        if (this.getData() != null) {
            pageResult.setData(
                    this.getData().stream()
                            .map(action::convert)
                            .collect(Collectors.toList())
            );
        }

        return pageResult;
    }

    @FunctionalInterface
    public interface ConvertAction<V, T> {
        V convert(T t);
    }

}
