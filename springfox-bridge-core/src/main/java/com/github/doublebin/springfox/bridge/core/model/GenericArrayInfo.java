package com.github.doublebin.springfox.bridge.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by juyan on 2019/3/14.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenericArrayInfo {

    /**
     * 可能性:1,GenericArrayInfo:多维数组;2,GenericInfo;3,String:泛型定义;4,具体类
     */
    private Object info;
}
