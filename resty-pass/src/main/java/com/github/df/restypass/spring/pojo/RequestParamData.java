package com.github.df.restypass.spring.pojo;

import lombok.Data;

/**
 * @RequestParam content
 * Created by darrenfu on 17-7-27.
 */
@Data
public class RequestParamData {

    private Integer index;

    private String name;

    private boolean required;

    private String defaultValue;

}
