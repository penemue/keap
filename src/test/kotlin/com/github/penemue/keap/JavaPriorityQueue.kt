/**
 * Copyright 2016 - 2019 Vyacheslav Lukianov (https://github.com/penemue)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.penemue.keap

import java.lang.reflect.Method
import java.util.*
import java.util.PriorityQueue

/**
 * Inheritor of [java.util.PriorityQueue] with constructor applying [java.util.Comparator] and [java.util.Collection].
 */
class JavaPriorityQueue<T>(cmp: Comparator<in T>? = null, c: Collection<T>? = null) : PriorityQueue<T>() {

    init {
        val superclass = javaClass.superclass
        val field = superclass.getDeclaredField("comparator")
        field.isAccessible = true
        field.set(this, cmp)
        if (c != null) {
            var initMethod: Method? = null
            superclass.declaredMethods.filter { it.name == "initFromCollection" }.forEach {
                it.isAccessible = true
                initMethod = it
            }
            initMethod!!.invoke(this, c)
        }
    }
}