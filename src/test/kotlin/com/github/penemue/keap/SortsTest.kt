package com.github.penemue.keap

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

inline fun <reified T> Collection<T>.heapSort(cmp: Comparator<in T>? = null): Array<T> {
    val heap = JavaPriorityQueue(cmp, this)
    val result = arrayOfNulls<T>(heap.size)
    for (i in result.indices) {
        result[i] = heap.poll()
    }
    @Suppress("UNCHECKED_CAST")
    return result as Array<T>
}

inline fun <reified T> Collection<T>.jvmSort(cmp: Comparator<in T>): Array<T> {
    val sorted = this.toTypedArray()
    Arrays.sort(sorted, cmp)
    return sorted
}

class SortsTest {

    @Test
    fun keapSortRandomStrings() {
        testSort("keapSort", 1000, { collection, cmp -> collection.keapSort(cmp) })
        testSort("keapSort", 10000, { collection, cmp -> collection.keapSort(cmp) })
        testSort("keapSort", 100000, { collection, cmp -> collection.keapSort(cmp) })
        testSort("keapSort", 1000000, { collection, cmp -> collection.keapSort(cmp) })
        testSort("keapSort", 2000000, { collection, cmp -> collection.keapSort(cmp) })
        testSort("keapSort", 4000000, { collection, cmp -> collection.keapSort(cmp) })
    }

    @Test
    fun heapSortRandomStrings() {
        testSort("heapSort", 1000, { collection, cmp -> collection.heapSort(cmp) })
        testSort("heapSort", 10000, { collection, cmp -> collection.heapSort(cmp) })
        testSort("heapSort", 100000, { collection, cmp -> collection.heapSort(cmp) })
        testSort("heapSort", 1000000, { collection, cmp -> collection.heapSort(cmp) })
        testSort("heapSort", 2000000, { collection, cmp -> collection.heapSort(cmp) })
        testSort("heapSort", 4000000, { collection, cmp -> collection.heapSort(cmp) })
    }

    @Test
    fun jvmSortRandomStrings() {
        val sortName = if (java.lang.Boolean.getBoolean("java.util.Arrays.useLegacyMergeSort")) "mergeSort" else "timSort"
        testSort(sortName, 1000, { collection, cmp -> collection.jvmSort(cmp) })
        testSort(sortName, 10000, { collection, cmp -> collection.jvmSort(cmp) })
        testSort(sortName, 100000, { collection, cmp -> collection.jvmSort(cmp) })
        testSort(sortName, 1000000, { collection, cmp -> collection.jvmSort(cmp) })
        testSort(sortName, 2000000, { collection, cmp -> collection.jvmSort(cmp) })
        testSort(sortName, 4000000, { collection, cmp -> collection.jvmSort(cmp) })
    }

    private fun testSort(sortName: String, inputSize: Int, sort: (Collection<String>, Comparator<String>) -> Array<String>) {
        val cmp = CountingComparator<String>(Comparator(String::compareTo))
        val sorted = sort(randomStringListOfSize(inputSize), cmp)
        for (i in 1 until sorted.size) {
            assertTrue(sorted[i - 1] <= sorted[i])
        }
        println("$sortName: input size = $inputSize, number of comparisons = ${cmp.count}")
    }

    private companion object RandomStrings {

        private val r = Random()

        fun randomStringListOfSize(size: Int): List<String> {
            val result = mutableListOf<String>()
            for (i in 0 until size) {
                result.add(Math.abs(r.nextLong()).toString())
            }
            return result
        }
    }
}