package com.github.df.restypass.test.service;

import com.github.df.restypass.test.entity.Response;
import com.github.df.restypass.test.entity.User;
import com.github.df.restypass.exception.execute.RestyException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 说明:
 * <p/>
 * Copyright: Copyright (c)
 * <p/>
 * Company:
 * <p/>
 *
 * @author darren-fu
 * @version 1.0.0
 * @contact 13914793391
 * @date 2016/11/22
 */
@Slf4j
public class ProxyServiceImpl implements ProxyService {
    @Override
    public void getNothing() {
        log.error("Fallback nothing");
    }

    @Override
    public String getString() {
        log.error("Fallback String");
        return "Fallback String";
    }

    public String getString(RestyException ex) {
        System.out.println("执行加强降级，Ex:" + ex.getMessage());
        log.error("FallbackException:{}", ex.getMessage());
        return "FallbackException String";
    }


    @Override
    public User getUser() {
        User user = new User();
        user.setName("Fallback");

        return user;
    }


    @Override
    public List<User> getList() {
        return null;
    }

    @Override
    public Response<String> getAge(Long id, String code, String name) {
        return null;
    }

    @Override
    public int getHeight(Long id) {
        return 0;
    }

    @Override
    public String update(Long id, String name, User user) {
        System.out.println("执行基本降级");

        return "FALLBACK";
    }

    @Override
    public void applicationIndex() {

    }
}
