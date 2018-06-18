/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging.core

import java.lang.reflect.Field
/**
 * (С) RGS Group, http://www.rgs.ru
 * @author jihor (dmitriy_zhikharev@rgs.ru)
 * Created on 2016-03-30
 */
trait Validatable {
    void validate() {
        def errors = []
        getClass().declaredFields.each {
            def error = validate(it)
            if (error) {
                errors << error
            }
        }
        if (errors) {
            throw new RuntimeException("$errors")
        }
    }

    String validate(Field field) {
        def constraint = field.getAnnotation(Constraint)
        constraint ? validate(field, constraint.value()) : null
    }

    String validate(Field field, Class constraintClass) {
        field.setAccessible(true)
        def closure = constraintClass.newInstance(null, null)
        def fieldValue = field.get(this)
        if (!closure.call(fieldValue) == true) {
            return "Field ${field.name} has invalid value: $fieldValue"
        }
    }
}