/**
 * Copyright 2016 Vyacheslav Lukianov (https://github.com/penemue)
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

import java.util.*

inline fun <reified T> Collection<T>.keapSorted(cmp: Comparator<in T>? = null): Iterable<T> {
    return SortedIterable(this, cmp)
}

inline fun <reified T> Collection<T>.keapSort(cmp: Comparator<in T>? = null): Array<T> {
    val result = arrayOfNulls<T>(size)
    keapSorted(cmp).forEachIndexed { i, it -> result[i] = it }
    @Suppress("UNCHECKED_CAST")
    return result as Array<T>
}

inline fun <reified T> Array<T>.keapSort(cmp: Comparator<in T>? = null): Array<T> {
    return asList().keapSort(cmp)
}