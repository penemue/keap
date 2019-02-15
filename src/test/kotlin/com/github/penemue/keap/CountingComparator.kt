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

import java.util.*

/**
 * [Comparator][java.util.Comparator] that counts number of invocations of [compare].
 */
internal class CountingComparator<T>(private val comparator: Comparator<T>) : Comparator<T> {

    internal var count = 0

    override fun compare(o1: T, o2: T): Int {
        ++count
        return comparator.compare(o1, o2)
    }
}