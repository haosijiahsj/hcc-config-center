package com.hcc.config.center.server.config;

import com.hcc.config.center.domain.result.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;
import java.text.MessageFormat;

/**
 * WebAdvice
 *
 * @author hushengjun
 * @date 2022/10/6
 */
@Slf4j
@ControllerAdvice
public class WebAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        Method method = methodParameter.getMethod();
        if (method == null) {
            return true;
        }
        if (method.getAnnotation(IgnoreRestResult.class) != null) {
            log.info("方法：{}，包含注解IgnoreRestResult, 不进行RestResult包装", method.getName());
            return false;
        }
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.getAnnotation(IgnoreRestResult.class) != null) {
            log.info("类：{}，包含注解IgnoreRestResult, 不进行RestResult包装", declaringClass.getName());
            return false;
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (o instanceof RestResult) {
            return o;
        }
        RestResult<Object> result = new RestResult<>();
        result.setCode(0);
        result.setSuccess(true);
        result.setMessage("success");
        result.setData(o);

        return result;
    }

    @ExceptionHandler
    public RestResult<?> handleException(Throwable e) {
        log.error("访问异常", e);
        String message = e.getMessage();

        RestResult<Object> result = new RestResult<>();
        result.setCode(-1);
        result.setSuccess(false);
        result.setMessage(message);

        return result;
    }

}
