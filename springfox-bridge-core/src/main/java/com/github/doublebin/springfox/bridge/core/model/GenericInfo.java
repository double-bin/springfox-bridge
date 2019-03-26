package com.github.doublebin.springfox.bridge.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenericInfo {

    private Class clazz;

    /**
     * 三种情况:1:string,泛型名;2,Class:具体类(包含具体的数组类);3,GenericInfo:GenericInfo对象;GenericArrayInfo:泛型数组
     */
    private List features;
}
