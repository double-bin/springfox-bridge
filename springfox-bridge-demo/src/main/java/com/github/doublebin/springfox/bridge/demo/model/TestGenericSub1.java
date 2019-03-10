package com.github.doublebin.springfox.bridge.demo.model;

import com.github.doublebin.springfox.bridge.core.handler.GenericSubClassHandler;
import com.github.doublebin.springfox.bridge.core.util.JsonUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestGenericSub1 extends TestGenericSub<String, String> {
    private String isData;

    private String sdata;

    @ApiModelProperty(value = "数据体", required = true)
    private TestResult pdata;

    private CommonResponse<TestResult, String> cData;

    private CommonResponse<String, String> tData;

    public static void main(String[] args) {

        String genericSignature = "Lcom/github/doublebin/springfox/bridge/demo/model/TestGeneric<Ljava/lang/String;TSS;Lcom/github/doublebin/springfox/bridge/demo/model/TestGeneric<TSS;Ljava/lang/String;Ljava/lang/Integer;>;>;";

        GenericSubClassHandler handler = new GenericSubClassHandler();
        handler.getGenericFeature(genericSignature);


    }
}
