/**
 * Copyright 2016 - 2017 Vyacheslav Lukianov (https://github.com/penemue)
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

import com.github.penemue.keap.PriorityQueue.Companion.MIN_CAPACITY
import com.github.penemue.keap.RandomStrings.randomStringListOfSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class PriorityQueueAuxTests {

    @Test
    fun keapifyRandom() {
        testKeapify(randomStringListOfSize(100000))
    }

    @Test
    fun keapifyOrderedAsc() {
        testKeapify(randomStringListOfSize(100000).keapSort().asList())
    }

    @Test
    fun keapifyOrderedDesc() {
        testKeapify(randomStringListOfSize(100000).keapSort().asList().asReversed())
    }

    @Test
    fun minCapacity() {
        assertEquals(MIN_CAPACITY, PriorityQueue<Int>().capacity)
        repeat(MIN_CAPACITY - 1, {
            assertEquals(MIN_CAPACITY, PriorityQueue<Int>(it + 1).capacity)
        })
    }

    @Test
    fun stability() {
        val q = PriorityQueue<StableComparable>()
        val rnd = Random()
        repeat(100000, { q.offer(StableComparable(rnd.nextInt(100))) })
        testStability(100000, q)
    }

    @Test
    fun stabilityOfKeapified() {
        val rnd = Random()
        val rndList = ArrayList<StableComparable>()
        repeat(100000, { rndList.add(StableComparable(rnd.nextInt(100))) })
        testStability(100000, rndList.keapify())
    }

    @Test
    fun randomLoad() {
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
            repeat(10000, { q.offer(randomStrings[it]) })
            var i = q.size
            while (i < randomStrings.size) {
                repeat((i % 10) + 1, {
                    if (i < randomStrings.size) {
                        q.offer(randomStrings[i++])
                    }
                })
                repeat((i % 10) + 1, { q.poll() })
            }
        }
    }
}