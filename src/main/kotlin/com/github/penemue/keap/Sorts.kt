/**
 * Copyright 2016 - 2024 Vyacheslav Lukianov (https://github.com/penemue)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.penemue.keap

/**
 * For an [iterable][Iterable], returns [iterable][Iterable] of elements sorted according to their
 * [natural ordering][Comparable] or by the specified [comparator][cmp].
 * If `size` is the size of this, then the first (the least) item is returned after `size - 1` comparisons.
 */
inline fun <reified T> Iterable<T>.keapSorted(cmp: Comparator<in T>? = null): Iterable<T> {
    return SortedIterable(this, cmp)
}

/**
 * For a [collection][Collection], returns [array][Array] of elements sorted according to their
 * [natural ordering][Comparable] or by the specified [comparator][cmp].
 */
inline fun <reified T> Collection<T>.keapSort(cmp: Comparator<in T>? = null): Array<T> {
    val result = arrayOfNulls<T>(size)
    keapSorted(cmp).forEachIndexed { i, it -> result[i] = it }
    @Suppress("UNCHECKED_CAST")
    return result as Array<T>
}

/**
 * For an [array][Collection], returns [array][Iterable] of elements sorted according to their
 * [natural ordering][Comparable] or by the specified [comparator][cmp].
 */
inline fun <reified T> Array<T>.keapSort(cmp: Comparator<in T>? = null): Array<T> {
    return asList().keapSort(cmp)
}