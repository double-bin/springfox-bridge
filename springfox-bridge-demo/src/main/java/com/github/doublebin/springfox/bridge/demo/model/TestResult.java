package com.github.doublebin.springfox.bridge.demo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "测试返回体")
public class TestResult {
    @ApiModelProperty(value = "名字", required = true)
    private String name;

    @ApiModelProperty(value = "年龄", required = true)
    private int age;
}
