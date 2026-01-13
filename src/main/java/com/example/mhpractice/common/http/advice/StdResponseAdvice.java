package com.example.mhpractice.common.http.advice;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.example.mhpractice.common.http.annotation.StandardReponseBody;

import java.lang.reflect.AnnotatedElement;
import org.springframework.core.io.Resource;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

@RestControllerAdvice
public class StdResponseAdvice implements ResponseBodyAdvice<Object> {

    /**
     * Method that checks if the method is annotated with @StandardReponseBody
     * 
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {

        // Don't wrap binary files (images, PDFs, etc.)
        Class<?> parameterType = returnType.getParameterType();
        if (Resource.class.isAssignableFrom(parameterType)) {
            return false;
        }

        // AnnotatedElementUtils - Spring utility class for working with annotations
        // hasAnnotation(element, annotationClass) - Returns true if annotation present
        // returnType.getDeclaringClass() - Returns the class that declares the method
        boolean hasClassAnnotation = AnnotatedElementUtils.hasAnnotation(returnType.getDeclaringClass(),
                StandardReponseBody.class);
        boolean hasMethodAnnotation = AnnotatedElementUtils.hasAnnotation(returnType.getMethod(),
                StandardReponseBody.class);

        return hasClassAnnotation || hasMethodAnnotation;
    }

    /**
     * Method that is called before the response body is written
     * 
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {

        if (body instanceof StandardResponse) {
            return body;
        }

        return StandardResponse.success(body);
    }
}