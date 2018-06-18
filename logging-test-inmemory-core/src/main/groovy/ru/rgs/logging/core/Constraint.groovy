/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging.core

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * (С) RGS Group, http://www.rgs.ru
 * @author jihor (dmitriy_zhikharev@rgs.ru)
 * Created on 2016-03-30
 */
@Retention(RetentionPolicy.RUNTIME)
@interface Constraint {
    /*
    * Chapter 9 of JLS has restrictions for the return type of a method declared in an annotation type so we cannot put Closure here, only Class.
    * see https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html#jls-9.6.1
    * */

    Class value()
}

