package com.github.df.restypass.http.converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.df.restypass.util.JsonTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Json类型响应解析类
 * Created by darrenfu on 17-7-19.
 */
public class JsonResponseConverter implements ResponseConverter<Object> {

    private static final Logger log = LoggerFactory.getLogger(JsonResponseConverter.class);

    private static final String APPLICATION_JSON = "application/json";

    private ObjectMapper objectMapper;

    public JsonResponseConverter() {
        this.objectMapper = JsonTools.defaultMapper().getMapper();
    }

    @Override
    public boolean support(Type type, String contentType) {
        if (contentType == null) {
            return false;
        }
        if (contentType.startsWith(APPLICATION_JSON) && !type.getTypeName().equals("void")) {
            return true;
        }
        return false;
    }

    @Override
    public Object convert(byte[] body, Type type, String contentType) {
        JavaType javaType = TypeFactory.defaultInstance().constructType(type);
        try {
            return objectMapper.readValue(body, javaType);
        } catch (IOException e) {
            log.error("JSON转换失败,javaType:{}", javaType);
            e.printStackTrace();
        }
        return null;
    }
}
