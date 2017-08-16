package com.github.df.restypass.spring.pojo;

import lombok.Data;


/**
 * @RequestHeader content
 * Created by darrenfu on 17-7-28.
 */
@Data
public class RequestHeaderData {

    private Integer index;
    private String name;
    private boolean required;
    private String defaultValue = null;

}
