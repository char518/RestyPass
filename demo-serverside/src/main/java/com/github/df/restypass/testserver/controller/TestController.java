package com.github.df.restypass.testserver.controller;

import com.github.df.restypass.testserver.entity.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Future;

/**
 * Created by darrenfu on 17-7-29.
 */
@RestController
@RequestMapping("/resty")
public class TestController {
    @RequestMapping(value = "/get_nothing", method = RequestMethod.GET)
    public void nothing() {
        System.out.println("############nothing");
    }

    @RequestMapping(value = "/get_status", method = RequestMethod.GET)
    public String getStatus() throws InterruptedException {

        Thread.sleep(5000);
        return "Status is OK";
    }


    @RequestMapping(value = "/get_user", method = RequestMethod.GET)
    public User getUser() throws InterruptedException {
        System.out.println("get user called");
        Thread.sleep(5000);
        System.out.println("get user finished!");
        return new User("test_user");
    }

    @RequestMapping(value = "/get_void_async", method = RequestMethod.GET)
    void getVoidAsync() {
        System.out.println("void method called");
    }

}
