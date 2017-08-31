package com.github.df.restypass.spring.wrapper;

import com.github.df.restypass.command.RestyRequestTemplate;

import java.lang.annotation.Annotation;

/**
 * 注解解析器
 * Created by darrenfu on 17-7-19.
 */
@SuppressWarnings("unused")
public interface SpringAnnotationProcessor {

    /**
     * 获取注解Class
     *
     * @return the annotation type
     */
    Class<? extends Annotation> getAnnotationType();

    /**
     * 处理Spring注解,数据填充requestTemplate
     *
     * @param requestTemplate the request template
     * @param annotation      the annotation
     * @return the boolean
     */
    @SuppressWarnings("UnusedReturnValue")
    boolean process(RestyRequestTemplate requestTemplate, Annotation annotation);

}
