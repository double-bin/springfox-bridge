package com.github.doublebin.springfox.bridge.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenericArrayInfo {
    /**
     *
     * four types:1: string,generic name; 2,Class: concrete class; 3,GenericInfo: GenericInfo object; 4,GenericArrayInfo: GenericArrayInfo object
     */
    private Object info;
}
