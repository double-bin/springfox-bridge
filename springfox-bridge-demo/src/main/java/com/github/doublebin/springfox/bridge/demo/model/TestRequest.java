package com.github.doublebin.springfox.bridge.demo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(value="TestRequest", description = "测试请求体,desc界面不展示")
@Getter
@Setter
public class TestRequest {

    @ApiModelProperty(value = "唯一id", required = true)
    private long uuid;

    @ApiModelProperty(value = "名字", required = true)
    private String name;
}
