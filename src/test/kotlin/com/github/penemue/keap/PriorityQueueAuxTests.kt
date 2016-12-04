package com.github.penemue.keap

import com.github.penemue.keap.PriorityQueue.KeapMiscellaneous.MIN_CAPACITY
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
    }
}