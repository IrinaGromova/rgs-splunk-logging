/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging.core
/**
 * (С) RGS Group, http://www.rgs.ru
 * @author jihor (dmitriy_zhikharev@rgs.ru)
 * Created on 2016-03-30
 */


class CanonicalLogEvent implements Validatable {
    @Constraint({ it })
    String System

    @Constraint({ it })
    String Environment

    String BusinessOperationCode

    String BusinessOperationName

    @Constraint({ it })
    String Node

    @Constraint({ it })
    String Action

    String ActionId

    // simple "it" means value must be groovy truthy (not null, not empty etc.)
    @Constraint({ it })
    String CorrelationId

    @Constraint({it in ["Started", "Completed", "Failed"] })
    String Message

    Map<String, String> ext
}