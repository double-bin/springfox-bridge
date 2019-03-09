package com.github.doublebin.springfox.bridge.demo.model;

import com.sun.org.apache.xpath.internal.operations.Bool;
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
@ApiModel(value="TestGeneric<TestRequest1>", description = "返回泛型")
public class TestGenericSub<TT,SS> extends TestGeneric<SS, String> {

    private TT tdata;

    private SS sdata;

    @ApiModelProperty(value = "数据体", required = true)
    private TestResult pdata;



    //boolean: getter:都以is开头,setter:都以set开头,后面不接Is
    //Boolean: getter:都以get开头,后面不接Is, setter:都以set开头,后面不接Is

   /* public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public Boolean getTest() {
        return isTest;
    }

    public void setTest(Boolean test) {
        isTest = test;
    }

    public Boolean getNeed() {
        return need;
    }

    public void setNeed(Boolean need) {
        this.need = need;
    }*/
}
