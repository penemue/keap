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

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class ComparatorFailureTests {

    @Test
    fun `failed offer leaves the queue unchanged`() {
        for (n in 0..MAX_SIZE) {
            checkFailureAtEveryComparison({ queueOf(n, it) }) { it.offer(0) }
            checkFailureAtEveryComparison({ queueOf(n, it) }) { it.offer(n / 2) }
            checkFailureAtEveryComparison({ sparseQueueOf(n, it) }) { it.offer(0) }
        }
    }

    @Test
    fun `failed poll leaves the queue unchanged`() {
        for (n in 1..MAX_SIZE) {
            checkFailureAtEveryComparison({ queueOf(n, it) }) { it.poll() }
            checkFailureAtEveryComparison({ sparseQueueOf(n, it) }) { it.poll() }
        }
    }

    @Test
    fun `failed remove of an element leaves the queue unchanged`() {
        for (n in 1..MAX_SIZE) {
            for (element in 1..n) {
                checkFailureAtEveryComparison({ queueOf(n, it) }) { it.remove(element) }
            }
        }
    }

    @Test
    fun `failed iterator remove leaves the queue unchanged`() {
        for (n in 1..MAX_SIZE) {
            for (position in 0 until n) {
                checkFailureAtEveryComparison({ queueOf(n, it) }) {
                    val iterator = it.iterator()
                    repeat(position + 1) { iterator.next() }
                    iterator.remove()
                }
            }
        }
    }

    /**
     * Runs [operation] on queues created by [create], with the comparator failing after 0, 1, 2, ...
     * comparisons, until the operation succeeds. After each failure, checks that the queue holds the same
     * elements as before and that it still accepts elements and polls them in order.
     */
    private fun checkFailureAtEveryComparison(
        create: (FailingComparator) -> PriorityQueue<Int>,
        operation: (PriorityQueue<Int>) -> Unit
    ) {
        for (allowedComparisons in 0..Int.MAX_VALUE) {
            val cmp = FailingComparator()
            val queue = create(cmp)
            val elements = queue.sorted()
            cmp.comparisonsLeft = allowedComparisons
            val failure = runCatching { operation(queue) }.exceptionOrNull() ?: return
            if (failure !is ComparatorFailure) throw failure
            cmp.comparisonsLeft = Int.MAX_VALUE

            val context = "failure after $allowedComparisons comparisons"
            assertEquals(context, elements.size, queue.size)
            assertEquals(context, elements, queue.sorted())
            queue.offer(-1)
            assertEquals(context, listOf(-1) + elements, generateSequence { queue.poll() }.toList())
        }
    }

    private fun queueOf(n: Int, cmp: FailingComparator) =
        PriorityQueue(cmp).apply { (1..n).shuffled(Random(n)).forEach { offer(it) } }

    /**
     * Creates a queue whose elements occupy few of its slots, so that the next poll compacts it
     * and the next offer can reuse its storage.
     */
    private fun sparseQueueOf(n: Int, cmp: FailingComparator) =
        queueOf(4 * n, cmp).apply { (1..4 * n).filter { it % 4 != 0 }.forEach { remove(it) } }

    private class FailingComparator : Comparator<Int> {

        var comparisonsLeft = Int.MAX_VALUE

        override fun compare(o1: Int, o2: Int): Int {
            if (comparisonsLeft-- <= 0) throw ComparatorFailure()
            return o1.compareTo(o2)
        }
    }

    private class ComparatorFailure : RuntimeException()

    private companion object {
        const val MAX_SIZE = 40
    }
}
