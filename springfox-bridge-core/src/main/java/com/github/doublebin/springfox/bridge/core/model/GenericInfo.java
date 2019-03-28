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
     *
     * four types:1: string,generic name; 2,Class: concrete class (including array class); 3,GenericInfo: GenericInfo object; 4,GenericArrayInfo: GenericArrayInfo object
     */
    private List features;
}
