package com.github.df.restypass.http.client;

import lombok.Data;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClient;

/**
 * Http Client Config 包装类
 * Created by darrenfu on 17-6-24.
 */
@Data
public class HttpClientWrapper {

    private AsyncHttpClient client;

    private AsyncHttpClientConfig config;

    public HttpClientWrapper(AsyncHttpClientConfig config) {
        this.client = new DefaultAsyncHttpClient(config);
        this.config = config;
    }

    public HttpClientWrapper(AsyncHttpClient client, AsyncHttpClientConfig config) {
        this.client = client;
        this.config = config;
    }
}
