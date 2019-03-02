package com.github.doublebin.springfox.bridge.demo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="TestRequest1", description = "测试请求体1")
public class TestRequest1 {

    @ApiModelProperty(value = "唯一id", required = true)
    private long uuid;

    @ApiModelProperty(value = "名字", required = true)
    private String name;
}
