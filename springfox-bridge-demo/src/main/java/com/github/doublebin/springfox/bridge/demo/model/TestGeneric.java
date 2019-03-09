package com.github.doublebin.springfox.bridge.demo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by juyan on 2019/3/6.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value="TestGeneric", description = "测试泛型")
public class TestGeneric<TT, BB> {

    @ApiModelProperty(value = "数据体", required = true)
    private TT data;

    @ApiModelProperty(value = "名称", required = true)
    private BB name;
}
