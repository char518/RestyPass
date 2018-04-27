package com.github.df.restypass.http.converter;

import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.exception.execute.RequestException;
import com.github.df.restypass.exception.execute.RestyException;
import com.github.df.restypass.exception.execute.ServerException;
import com.github.df.restypass.http.pojo.FailedResponse;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 响应解析
 * Created by darrenfu on 17-7-20.
 */
@SuppressWarnings("AlibabaUndefineMagicConstant")
public class ResponseConverterContext {

    public static ResponseConverterContext DEFAULT = new ResponseConverterContext();

    private static final Logger log = LoggerFactory.getLogger(ResponseConverterContext.class);
    /**
     * 转换器列表
     */
    private List<ResponseConverter> converterList;

    /**
     * Instantiates a new Response converter context.
     */
    public ResponseConverterContext() {
        this.converterList = new ArrayList<>();
        converterList.add(new VoidResponseConverter());
        converterList.add(new JsonResponseConverter());
        converterList.add(new StringResponseConverter());
    }

    /**
     * Instantiates a new Response converter context.
     *
     * @param converterList the converter list
     */
    public ResponseConverterContext(List<ResponseConverter> converterList) {
        this.converterList = converterList;
    }


    /**
     * Convert response object.
     *
     * @param restyCommand the resty command
     * @param response     the response
     * @return the object
     */
    public Object convertResponse(RestyCommand restyCommand, Response response) {

        Object result = null;
        // response 为null
        if (response == null) {
            log.warn("response is null,command:{}", restyCommand);
            restyCommand.failed(new ServerException("Failed to get response, it's null"));
            return result;
        }

        // response为FailedResponse， [connectException InterruptedException]  status 500
        if (FailedResponse.isFailedResponse(response)) {
            log.warn("response is failed by exception:{}", FailedResponse.class.cast(response).getException().getMessage(), FailedResponse.class.cast(response).getException());
            restyCommand.failed(FailedResponse.class.cast(response).getException());
            return result;
        }

        // 服务响应了请求，但是不是200
        int statusCode = response.getStatusCode();
        if (200 != statusCode) {
            if (statusCode >= 400 && statusCode < 500) {
                restyCommand.failed(new RequestException("Request: " + restyCommand + "; Response: " + response.toString()));
            } else if (statusCode >= 500) {
                restyCommand.failed(new ServerException("Request: " + restyCommand + "; Response: " + response.toString()));
            }

            log.warn("request is bad,status code:{},request:{},response:{}", statusCode, restyCommand, response);
            return result;
        }


        byte[] body = response.getResponseBodyAsBytes();

        // 使用转换器 转换响应结果   json->object
        boolean converted = false;
        Type returnType = restyCommand.getReturnType();
        String respContentType = response.getContentType();
        for (ResponseConverter converter : converterList) {
            if (converter.support(returnType, respContentType)) {
                converted = true;
                result = converter.convert(body, returnType, respContentType);
                break;
            }
        }
        if (!converted) {
            restyCommand.failed(new RestyException("没有合适的解析器,content-type:" + respContentType + ";body:" + response.getResponseBody()));
            log.warn("没有合适的解析器,content-type:{};body:{}", respContentType, response.getResponseBody());

        }
        restyCommand.success();
        return result;
    }

}
