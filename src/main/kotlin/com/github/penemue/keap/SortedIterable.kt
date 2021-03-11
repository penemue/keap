/**
 * Copyright 2016 - 2021 Vyacheslav Lukianov (https://github.com/penemue)
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

/**
 * Iterates over the elements of the specified [collection][c]. Returned elements are sorted according to their
 * [natural ordering][Comparable] or by the specified [comparator][cmp].
 */
open class SortedIterable<out T>(private val c: Iterable<T>, private val cmp: Comparator<in T>? = null) : Iterable<T> {

    override fun iterator(): Iterator<T> {
        val keap = (c as? PriorityQueue)?.copyOf() ?: c.keapify(cmp)
        return object : Iterator<T> {

            override fun next(): T {
                return keap.pollRaw() ?: throw NullPointerException()
            }

            override fun hasNext(): Boolean {
                return keap.isNotEmpty()
            }
        }
    }
}