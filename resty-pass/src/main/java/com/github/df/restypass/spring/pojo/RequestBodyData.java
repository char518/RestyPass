package com.github.df.restypass.spring.pojo;

import lombok.Data;


/**
 * @RequestBody content
 * Created by darrenfu on 17-7-28.
 */
@Data
public class RequestBodyData {

    private Integer index;
    private String name;
    private boolean required;
    private Object defaultValue = null;



}
