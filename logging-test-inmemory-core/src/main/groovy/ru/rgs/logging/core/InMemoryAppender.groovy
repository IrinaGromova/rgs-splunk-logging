/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging.core

/**
 * @author Nesterenko Maxim (maksim_nesterenko@rgs.ru)
 (c) RGS Group, http://www.rgs.ru
 Created on 3/1/17
 */
interface InMemoryAppender {
    void clearEvents()

    List<CanonicalLogEvent> getEvents()
}