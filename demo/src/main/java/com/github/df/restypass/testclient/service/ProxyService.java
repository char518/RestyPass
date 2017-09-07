package com.github.df.restypass.testclient.service;

import com.github.df.restypass.annotation.RestyMethod;
import com.github.df.restypass.annotation.RestyService;
import com.github.df.restypass.command.RestyFuture;
import com.github.df.restypass.lb.RoundRobinLoadBalancer;
import com.github.df.restypass.testclient.entity.Response;
import com.github.df.restypass.testclient.entity.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.Future;

/**
 * 说明:
 * <p/>
 * Copyright: Copyright (c)
 * <p/>
 * Company:
 * <p/>
 *
 * @author darren -fu
 * @version 1.0.0
 * @contact 13914793391
 * @date 2016 /11/22
 */
@RestyService(serviceName = "server",
        fallbackEnabled = true,
//        fallbackClass = ProxyServiceImpl.class,
        fallbackBean = "ProxyServiceImpl",
        forceBreakEnabled = false,
        circuitBreakEnabled = false,
        loadBalancer = RoundRobinLoadBalancer.class,
        retry = 1,
        requestTimeout = 10000,
        limit = 1000,
        version = {">=1.3.1-RELEASE"}
)
@RequestMapping(value = "/resty")
public interface ProxyService extends ApplicationService {

    /**
     * Gets nothing.
     */
// 同步调用
    @RestyMethod(retry = 1,
            fallbackEnabled = "true",
            circuitBreakEnabled = "false",
            forceBreakEnabled = "false",
            limit = 10)
    @RequestMapping(value = "/get_nothing", method = RequestMethod.GET, headers = "Client=RestyProxy", params = "Param1=val1")
    void getNothing();

    /**
     * Gets status.
     *
     * @param future the future
     * @return the status
     */
// 异步调用形式： Future<?> 参数类型，出参
    @RestyMethod
    @RequestMapping(value = "/get_status", method = RequestMethod.GET)
    String getStatus(RestyFuture<String> future);

    /**
     * Gets user.
     *
     * @return the user
     */
// 异步调用形式： Future<?> 返回类型
    @RestyMethod
    @RequestMapping(value = "/get_user", method = RequestMethod.GET)
    Future<User> getUser();

    /**
     * Gets void async.
     *
     * @return the void async
     */
    @RestyMethod
    @RequestMapping(value = "/get_void_async", method = RequestMethod.GET)
    Future<Void> getVoidAsync();


    /**
     * Gets list.
     *
     * @return the list
     */
    @RestyMethod()
    @RequestMapping(value = "/list")
    List<User> getList();

    /**
     * Gets age.
     *
     * @param id   the id
     * @param code the code
     * @param name the name
     * @return the age
     */
    @RestyMethod()
    @RequestMapping(value = "/get_age", method = RequestMethod.GET)
    Response<String> getAge(@RequestParam("id") Long id, String code, @PathVariable(value = "name") String name);

    /**
     * Gets height.
     *
     * @param id the id
     * @return the height
     */
    int getHeight(Long id);

    /**
     * Update string.
     *
     * @param id   the id
     * @param name the name
     * @param user the user
     * @return the string
     */
    @RestyMethod
    @RequestMapping(value = "/update/{name}", method = RequestMethod.POST)
    String update(@RequestParam("id") Long id, @PathVariable(value = "name") String name, User user);

}
