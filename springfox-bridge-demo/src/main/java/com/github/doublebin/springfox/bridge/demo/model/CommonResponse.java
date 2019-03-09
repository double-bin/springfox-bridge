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
@ApiModel(description = "通用响应对象")
public class CommonResponse<TM, S> {

    TM get() {
        return null;
    }

    @ApiModelProperty(value = "是否成功", required = true)
    private boolean success;

    @ApiModelProperty(value = "响应数据", required = true)
    private TM data;

    @ApiModelProperty(value = "模型", required = true)
    private S model;

    @ApiModelProperty(value = "结果码", required = true)
    private String code;

    @ApiModelProperty(value = "响应消息", required = true)
    private String message;
}
