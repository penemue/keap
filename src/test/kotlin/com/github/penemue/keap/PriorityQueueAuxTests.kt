package com.github.penemue.keap

import org.junit.Assert
import org.junit.Test
import java.util.*

class PriorityQueueAuxTests {

    @Test
    fun stability() {
        val q = PriorityQueue<StableComparable>()
        val rnd = Random()
        repeat(1000000, { q.offer(StableComparable(rnd.nextInt(100))) })

        Assert.assertEquals(1000000, q.size)

        var prev = q.poll()
        var current = q.poll()
        while (current != null) {
            if (current.compareTo(prev ?: throw NullPointerException()) == 0) {
                Assert.assertTrue(prev.index < current.index)
            }
            prev = current
            current = q.poll()
        }
    }

    private class StableComparable(var value: Int) : Comparable<StableComparable> {

        val index = counter++

        override fun compareTo(other: StableComparable): Int {
            return other.value.compareTo(value)
        }

        companion object {
            var counter = 0
        }
    }
}