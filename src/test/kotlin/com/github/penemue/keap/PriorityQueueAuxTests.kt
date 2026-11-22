/**
 * Copyright 2016 - 2026 Vyacheslav Lukianov (https://github.com/penemue)
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

import com.github.penemue.keap.PriorityQueue.Companion.MIN_CAPACITY
import com.github.penemue.keap.RandomStrings.randomStringListOfSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class PriorityQueueAuxTests {

    @Test
    fun `keapify of random input takes n - 1 comparisons`() {
        testKeapify(randomStringListOfSize(100000))
    }

    @Test
    fun `keapify of ascending input takes n - 1 comparisons`() {
        testKeapify(randomStringListOfSize(100000).keapSort().asList())
    }

    @Test
    fun `keapify of descending input takes n - 1 comparisons`() {
        testKeapify(randomStringListOfSize(100000).keapSort().asList().asReversed())
    }

    @Test
    fun `keapify of an empty iterable gives an empty queue of minimum capacity`() {
        assertTrue(emptyList<String>().keapify().isEmpty())
        assertTrue(emptySequence<String>().asIterable().keapify().isEmpty())
        assertEquals(MIN_CAPACITY, emptyList<String>().keapify().capacity)
    }

    @Test
    fun `keapify of a non-collection iterable uses the specified comparator`() {
        val keap = (1..6).keapify(reverseOrder())
        assertEquals(listOf(6, 5, 4, 3, 2, 1), generateSequence { keap.poll() }.toList())
        assertEquals(listOf(6, 5, 4, 3, 2, 1), (1..6).keapSorted(reverseOrder()).toList())
    }

    @Test
    fun `keapify of a non-collection iterable accepts non-comparable elements with a comparator`() {
        class Item(val key: Int)

        val keap = sequenceOf(3, 1, 2).map { Item(it) }.asIterable().keapify(compareBy { it.key })
        assertEquals(listOf(1, 2, 3), generateSequence { keap.poll() }.map { it.key }.toList())
    }

    @Test
    fun `capacity is never less than the minimum`() {
        assertEquals(MIN_CAPACITY, PriorityQueue<Int>().capacity)
        repeat(MIN_CAPACITY - 1) {
            assertEquals(MIN_CAPACITY, PriorityQueue<Int>(it + 1).capacity)
        }
    }

    @Test
    fun `queue filled by offer keeps equal elements in insertion order`() {
        val q = PriorityQueue<StableComparable>()
        val rnd = Random()
        repeat(100000) { q.offer(StableComparable(rnd.nextInt(100))) }
        testStability(100000, q)
    }

    @Test
    fun `keapified queue keeps equal elements in insertion order`() {
        val rnd = Random()
        val rndList = ArrayList<StableComparable>()
        repeat(100000) { rndList.add(StableComparable(rnd.nextInt(100))) }
        testStability(100000, rndList.keapify())
    }

    @Test
    fun `random mix of offers and polls completes for PriorityQueue and java#util#PriorityQueue`() {
        val randomStrings = RandomStrings.randomStringListOfSize(1000000)

        val jupqCmp = CountingComparator<String>(Comparator(String::compareTo))
        val jupq = java.util.PriorityQueue<String>(jupqCmp)
        testQueue(jupq, randomStrings)
        println("java.util.PriorityQueue did ${jupqCmp.count} comparisons")

        val pqCmp = CountingComparator<String>(Comparator(String::compareTo))
        val pq = PriorityQueue(pqCmp)
        testQueue(pq, randomStrings)
        println("PriorityQueue did ${pqCmp.count} comparisons")
    }

    companion object {

        private class StableComparable(var value: Int) : Comparable<StableComparable> {

            val index = counter++

            override fun compareTo(other: StableComparable): Int {
                return other.value.compareTo(value)
            }

            companion object {
                var counter = 0
            }
        }

        private fun testKeapify(input: List<String>) {
            val cmp = CountingComparator<String>(Comparator(String::compareTo))
            input.keapify(cmp)
            assertEquals(input.size - 1, cmp.count)
        }

        private fun testStability(expectedSize: Int, q: PriorityQueue<StableComparable>) {
            assertEquals(expectedSize, q.size)
            var prev = q.poll()
            var current = q.poll()
            while (current != null) {
                if (current.compareTo(prev ?: throw NullPointerException()) == 0) {
                    assertTrue(prev.index < current.index)
                }
                prev = current
                current = q.poll()
            }
        }

        private fun testQueue(q: Queue<String>, randomStrings: List<String>) {
            repeat(10000) { q.offer(randomStrings[it]) }
            var i = q.size
            while (i < randomStrings.size) {
                repeat((i % 10) + 1) {
                    if (i < randomStrings.size) {
                        q.offer(randomStrings[i++])
                    }
                }
                repeat((i % 10) + 1) { q.poll() }
            }
        }
    }
}