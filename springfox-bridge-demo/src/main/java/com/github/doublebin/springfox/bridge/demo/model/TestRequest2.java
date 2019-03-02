package com.github.doublebin.springfox.bridge.demo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="TestRequest2", description = "测试请求体2")
public class TestRequest2 {

    @ApiModelProperty(value = "名字", required = true)
    private String name;

    @ApiModelProperty(value = "描述", required = true)
    private String desc;
}
