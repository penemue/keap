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

import org.junit.Assert.*
import org.junit.Test
import java.io.*

const val SIZE = 1000

/**
 * PriorityQueue tests adapted from Doug Lea's tests for [java.util.PriorityQueue]:
 * https://svn.apache.org/repos/asf/harmony/standard/classlib/trunk/modules/concurrent/src/test/java/PriorityQueueTest.java.
 */
class PriorityQueueTests {

    /**
     * Create a queue of given size containing consecutive
     * Integers 0 ... n.
     */
    private fun populatedQueue(n: Int): PriorityQueue<Int> {
        val q = PriorityQueue<Int>(n)
        assertTrue(q.isEmpty())
        for (i in n - 1 downTo 0 step 2) assertTrue(q.offer(i))
        for (i in n % 2 until n step 2) assertTrue(q.offer(i))
        assertFalse(q.isEmpty())
        assertEquals(n, q.size)
        return q
    }

    @Test
    fun `default constructor creates an empty queue`() {
        assertEquals(0, PriorityQueue<Int>().size)
    }

    @Test
    fun `queue created with an initial capacity is empty`() {
        assertEquals(0, PriorityQueue<Int>(SIZE).size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero capacity throws IllegalArgumentException`() {
        PriorityQueue<Int>(0)
    }

    @Test(expected = NullPointerException::class)
    fun `creating from a collection of nulls throws NullPointerException`() {
        val ints = arrayOfNulls<Int>(SIZE)
        PriorityQueue(ints.asList())
    }

    @Test(expected = NullPointerException::class)
    fun `creating from a collection with a null element throws NullPointerException`() {
        val ints = arrayOfNulls<Int>(SIZE)
        for (i in 0 until SIZE - 1) {
            ints[i] = i
        }
        PriorityQueue(ints.asList())
    }

    @Test
    fun `queue created from a collection contains all its elements`() {
        val ints = Array(SIZE) { it }
        val q = PriorityQueue(ints.asList())
        for (i in 0 until SIZE) {
            assertEquals(ints[i], q.poll())
        }
    }

    @Test
    fun `queue created from an empty collection is empty and usable`() {
        val cmp = MyReverseComparator()
        val q = PriorityQueue(emptyList(), cmp)
        assertEquals(cmp, q.comparator())
        assertTrue(q.isEmpty())
        assertNull(q.peek())
        assertNull(q.poll())
        assertTrue(q.offer(1))
        assertTrue(q.offer(2))
        assertEquals(2, q.poll())
        assertEquals(1, q.poll())
        assertTrue(q.isEmpty())
    }

    @Test
    fun `comparator passed to the constructor orders the queue`() {
        val cmp = MyReverseComparator()
        val q = PriorityQueue(SIZE, cmp)
        assertEquals(cmp, q.comparator())
        val ints = Array(SIZE) { it }
        q.addAll(ints.asList())
        for (i in SIZE - 1 downTo 0) {
            assertEquals(ints[i], q.poll())
        }
    }

    @Test
    fun `isEmpty is true before add and after removing all elements`() {
        val q = PriorityQueue<Int>(2)
        assertTrue(q.isEmpty())
        q.add(1)
        assertFalse(q.isEmpty())
        q.add(2)
        q.remove()
        q.remove()
        assertTrue(q.isEmpty())
    }

    @Test
    fun `size changes as elements are added and removed`() {
        val q = populatedQueue(SIZE)
        for (i in 0 until SIZE) {
            assertEquals(SIZE - i, q.size)
            q.remove()
        }
        for (i in 0 until SIZE) {
            assertEquals(i, q.size)
            q.add(i)
        }
    }

    @Test
    fun `offer of a comparable element succeeds`() {
        val q = PriorityQueue<Int>(1)
        assertTrue(q.offer(0))
        assertTrue(q.offer(1))
    }

    @Test(expected = ClassCastException::class)
    fun `offer of non-comparable elements throws ClassCastException`() {
        val q = PriorityQueue<Any>(1)
        q.offer(Any())
        q.offer(Any())
        q.offer(Any())
    }

    @Test
    fun `add of a comparable element succeeds`() {
        val q = PriorityQueue<Int>(SIZE)
        for (i in 0 until SIZE) {
            assertEquals(i, q.size)
            assertTrue(q.add(i))
        }
    }

    @Test(expected = NullPointerException::class)
    fun `addAll of a collection of nulls throws NullPointerException`() {
        val q = PriorityQueue<Int?>(SIZE)
        val ints = arrayOfNulls<Int>(SIZE)
        q.addAll(ints.asList())
    }

    @Test(expected = NullPointerException::class)
    fun `addAll of a collection with a null element throws NullPointerException`() {
        val q = PriorityQueue<Int?>(SIZE)
        val ints = arrayOfNulls<Int>(SIZE)
        for (i in 0 until SIZE - 1)
            ints[i] = i
        q.addAll(ints.asList())
    }

    @Test
    fun `addAll adds all elements and reports whether the queue changed`() {
        val ints = Array(SIZE) { SIZE - 1 - it }
        val q = PriorityQueue<Int>(SIZE)
        assertFalse(q.addAll(emptyList()))
        assertTrue(q.addAll(ints.asList()))
        for (i in 0 until SIZE) {
            assertEquals(i, q.poll())
        }
    }

    @Test
    fun `poll returns elements in ascending order and null when empty`() {
        val q = populatedQueue(SIZE)
        for (i in 0 until SIZE) {
            assertEquals(i, (q.poll() as Int))
        }
        assertNull(q.poll())
    }

    @Test
    fun `peek returns the least element and null when empty`() {
        val q = populatedQueue(SIZE)
        for (i in 0 until SIZE) {
            assertEquals(i, (q.peek() as Int))
            q.poll()
            assertTrue(q.peek() == null || i != (q.peek() as Int))
        }
        assertNull(q.peek())
    }

    @Test(expected = NoSuchElementException::class)
    fun `element returns the least element and throws NoSuchElementException when empty`() {
        val q = populatedQueue(SIZE)
        for (i in 0 until SIZE) {
            assertEquals(i, (q.element() as Int))
            q.poll()
        }
        q.element()
    }

    @Test(expected = NoSuchElementException::class)
    fun `remove returns the least element and throws NoSuchElementException when empty`() {
        val q = populatedQueue(SIZE)
        for (i in 0 until SIZE) {
            assertEquals(i, (q.remove() as Int))
        }
        q.remove()
    }

    @Test(expected = NoSuchElementException::class)
    fun `remove throws NoSuchElementException after all elements are removed by value`() {
        val q = populatedQueue(SIZE)
        for (i in SIZE - 1 downTo 0) {
            assertTrue(q.remove(i))
            assertEquals(i, q.size)
        }
        q.remove()
    }

    @Test
    fun `remove of an element returns true only if the element is present`() {
        val q = populatedQueue(SIZE)
        run {
            var i = 1
            while (i < SIZE) {
                assertTrue(q.remove(i))
                i += 2
            }
        }
        var i = 0
        while (i < SIZE) {
            assertTrue(q.remove(i))
            assertFalse(q.remove(i + 1))
            i += 2
        }
        assertTrue(q.isEmpty())
    }

    @Test
    fun `contains is true for elements added and not yet removed`() {
        val q = populatedQueue(SIZE)
        for (i in 0 until SIZE) {
            assertTrue(q.contains(i))
            q.poll()
            assertFalse(q.contains(i))
        }
    }

    @Test
    fun `clear removes all elements`() {
        val q = populatedQueue(SIZE)
        q.clear()
        assertTrue(q.isEmpty())
        assertEquals(0, q.size)
        q.add(1)
        assertFalse(q.isEmpty())
        q.clear()
        assertTrue(q.isEmpty())
    }

    @Test
    fun `containsAll is true only for a subset of the elements`() {
        val q = populatedQueue(SIZE)
        val p = PriorityQueue<Int>(SIZE)
        for (i in 0 until SIZE) {
            assertTrue(q.containsAll(p))
            assertFalse(p.containsAll(q))
            p.add(i)
        }
        assertTrue(p.containsAll(q))
    }

    @Test
    fun `retainAll keeps only the given elements and reports whether the queue changed`() {
        val q = populatedQueue(SIZE)
        val p = populatedQueue(SIZE)
        for (i in 0 until SIZE) {
            val changed = q.retainAll(p)
            if (i == 0) {
                assertFalse(changed)
            } else {
                assertTrue(changed)
            }
            assertTrue(q.containsAll(p))
            assertEquals(SIZE - i, q.size)
            p.remove()
        }
    }

    @Test
    fun `removeAll removes only the given elements and reports whether the queue changed`() {
        for (i in 1 until SIZE) {
            val q = populatedQueue(SIZE)
            val p = populatedQueue(i)
            assertTrue(q.removeAll(p))
            assertEquals(SIZE - i, q.size)
            repeat(i) {
                assertFalse(q.contains(p.remove()))
            }
        }
    }

    @Test
    fun `toTypedArray contains all elements`() {
        val q = populatedQueue(SIZE)
        val o = q.toTypedArray()
        o.sort()
        for (i in o.indices)
            assertEquals(o[i], q.poll())
    }

    @Test
    fun `iterator visits every element`() {
        val q = populatedQueue(SIZE)
        var i = 0
        val it = q.iterator()
        while (it.hasNext()) {
            assertTrue(q.contains(it.next()))
            ++i
        }
        assertEquals(SIZE, i)
    }

    @Test
    fun `iterator remove deletes the current element`() {
        val q = PriorityQueue<Int>(3)
        q.add(1)
        q.add(2)
        q.add(3)

        var it: MutableIterator<*> = q.iterator()
        it.next()
        it.remove()

        it = q.iterator()
        assertEquals(2, it.next())
        assertEquals(3, it.next())
        assertFalse(it.hasNext())
    }

    @Test
    fun `toString contains every element`() {
        val q = populatedQueue(SIZE)
        val s = q.toString()
        for (i in 0 until SIZE) {
            assertTrue(s.indexOf(i.toString()) >= 0)
        }
    }

    @Test
    fun `deserialized queue has the same elements in the same order`() {
        val q = populatedQueue(SIZE)
        val r = serializedCopy(q)
        assertEquals(q.size, r.size)
        while (!q.isEmpty()) {
            assertEquals(q.remove(), r.remove())
        }
    }

    @Test
    fun `deserialized empty queue is empty and usable whether it was never filled or drained`() {
        val drained = populatedQueue(SIZE)
        repeat(SIZE) { drained.poll() }
        for (q in listOf(PriorityQueue(), drained)) {
            val r = serializedCopy(q)
            assertTrue(r.isEmpty())
            assertNull(r.peek())
            assertNull(r.poll())
            assertTrue(r.offer(2))
            assertTrue(r.offer(1))
            assertEquals(1, r.poll())
            assertEquals(2, r.poll())
            assertTrue(r.isEmpty())
        }
    }

    private fun <T> serializedCopy(q: PriorityQueue<T>): PriorityQueue<T> {
        val bout = ByteArrayOutputStream(10000)
        val output = ObjectOutputStream(BufferedOutputStream(bout))
        output.writeObject(q)
        output.close()

        val bin = ByteArrayInputStream(bout.toByteArray())
        val input = ObjectInputStream(BufferedInputStream(bin))
        @Suppress("UNCHECKED_CAST")
        return input.readObject() as PriorityQueue<T>
    }

    internal class MyReverseComparator : Comparator<Int> {

        override fun compare(x: Int, y: Int): Int {
            if (x < y) return 1
            if (x > y) return -1
            return 0
        }
    }
}
