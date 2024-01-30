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

import com.github.penemue.keap.RandomStrings.randomStringListOfSize
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

inline fun <reified T> Collection<T>.jvmSort(cmp: Comparator<in T>): Array<T> {
    return toTypedArray().apply { Arrays.sort(this, cmp) }
}

inline fun <reified T> Iterable<T>.keapSortIterable(cmp: Comparator<in T>): Array<T> {
    val heap = PriorityQueue(1000000, cmp)
    forEach { heap.offer(it) }
    val result = arrayOfNulls<T>(heap.size)
    repeat(heap.size) { result[it] = heap.poll() }
    @Suppress("UNCHECKED_CAST")
    return result as Array<T>
}

class SortsTest {

    @Test
    fun keapSortRandomStrings() {
        testSort("keapSort", 1000) { collection, cmp -> collection.keapSort(cmp) }
        testSort("keapSort", 10000) { collection, cmp -> collection.keapSort(cmp) }
        testSort("keapSort", 100000) { collection, cmp -> collection.keapSort(cmp) }
        testSort("keapSort", 1000000) { collection, cmp -> collection.keapSort(cmp) }
        testSort("keapSort", 2000000) { collection, cmp -> collection.keapSort(cmp) }
    }

    @Test
    fun keapSortRandomStrings2() {
        testSort("keapSortIterable", 1000) { collection, cmp -> collection.keapSortIterable(cmp) }
        testSort("keapSortIterable", 10000) { collection, cmp -> collection.keapSortIterable(cmp) }
        testSort("keapSortIterable", 100000) { collection, cmp -> collection.keapSortIterable(cmp) }
        testSort("keapSortIterable", 1000000) { collection, cmp -> collection.keapSortIterable(cmp) }
        testSort("keapSortIterable", 2000000) { collection, cmp -> collection.keapSortIterable(cmp) }
    }

    @Test
    fun jvmSortRandomStrings() {
        val sortName = if (java.lang.Boolean.getBoolean("java.util.Arrays.useLegacyMergeSort")) "mergeSort" else "timSort"
        testSort(sortName, 1000) { collection, cmp -> collection.jvmSort(cmp) }
        testSort(sortName, 10000) { collection, cmp -> collection.jvmSort(cmp) }
        testSort(sortName, 100000) { collection, cmp -> collection.jvmSort(cmp) }
        testSort(sortName, 1000000) { collection, cmp -> collection.jvmSort(cmp) }
        testSort(sortName, 2000000) { collection, cmp -> collection.jvmSort(cmp) }
    }

    companion object {

        private fun testSort(sortName: String, inputSize: Int, sort: (Collection<String>, Comparator<String>) -> Array<String>) {
            val cmp = CountingComparator<String>(Comparator(String::compareTo))
            val sorted = sort(randomStringListOfSize(inputSize), cmp)
            repeat(inputSize - 1) { assertTrue(sorted[it] <= sorted[it + 1]) }
            println("$sortName: input size = $inputSize, number of comparisons = ${cmp.count}")
        }
    }
}