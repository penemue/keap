package com.github.penemue.keap

import org.junit.Assert.*
import org.junit.Test
import java.io.*
import java.util.*

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

    /**
     * A new queue created by default constructor
     */
    @Test
    fun testConstructor0() {
        assertEquals(0, PriorityQueue<Int>().size)
    }

    /**
     * A new queue has unbounded capacity
     */
    @Test
    fun testConstructor1() {
        assertEquals(0, PriorityQueue<Int>(SIZE).size)
    }

    /**
     * Constructor throws IAE if  capacity argument nonpositive
     */
    @Test
    fun testConstructor2() {
        try {
            PriorityQueue<Int>(0)
            shouldThrow()
        } catch (success: IllegalArgumentException) {
        }

    }

    /**
     * Initializing from Collection of null elements throws NPE
     */
    @Test
    fun testConstructor4() {
        try {
            val ints = arrayOfNulls<Int>(SIZE)
            PriorityQueue(Arrays.asList<Int>(*ints))
            shouldThrow()
        } catch (success: NullPointerException) {
        }
    }

    /**
     * Initializing from Collection with some null elements throws NPE
     */
    @Test
    fun testConstructor5() {
        try {
            val ints = arrayOfNulls<Int>(SIZE)
            for (i in 0..SIZE - 1 - 1) {
                ints[i] = i
            }
            PriorityQueue(Arrays.asList<Int>(*ints))
            shouldThrow()
        } catch (success: NullPointerException) {
        }

    }

    /**
     * Queue contains all elements of collection used to initialize
     */
    @Test
    fun testConstructor6() {
        val ints = arrayOfNulls<Int>(SIZE)
        for (i in 0..SIZE - 1) {
            ints[i] = i
        }
        val q = PriorityQueue(Arrays.asList<Int>(*ints))
        for (i in 0..SIZE - 1) {
            assertEquals(ints[i], q.poll())
        }
    }

    /**
     * The comparator used in constructor is used
     */
    @Test
    fun testConstructor7() {
        val cmp = MyReverseComparator()
        val q = PriorityQueue(SIZE, cmp)
        assertEquals(cmp, q.comparator())
        val ints = arrayOfNulls<Int>(SIZE)
        for (i in 0..SIZE - 1) {
            ints[i] = i
        }
        q.addAll(Arrays.asList<Int>(*ints))
        for (i in SIZE - 1 downTo 0) {
            assertEquals(ints[i], q.poll())
        }
    }

    /**
     * isEmpty is true before add, false after
     */
    @Test
    fun testEmpty() {
        val q = PriorityQueue<Int>(2)
        assertTrue(q.isEmpty())
        q.add(1)
        assertFalse(q.isEmpty())
        q.add(2)
        q.remove()
        q.remove()
        assertTrue(q.isEmpty())
    }

    /**
     * size changes when elements added and removed
     */
    @Test
    fun testSize() {
        val q = populatedQueue(SIZE)
        for (i in 0..SIZE - 1) {
            assertEquals(SIZE - i, q.size)
            q.remove()
        }
        for (i in 0..SIZE - 1) {
            assertEquals(i, q.size)
            q.add(i)
        }
    }

    /**
     * Offer of comparable element succeeds
     */
    @Test
    fun testOffer() {
        val q = PriorityQueue<Int>(1)
        assertTrue(q.offer(0))
        assertTrue(q.offer(1))
    }

    /**
     * Offer of non-Comparable throws CCE
     */
    @Test
    fun testOfferNonComparable() {
        try {
            val q = PriorityQueue<Any>(1)
            q.offer(Any())
            q.offer(Any())
            q.offer(Any())
            shouldThrow()
        } catch (success: ClassCastException) {
        }
    }

    /**
     * add of comparable succeeds
     */
    @Test
    fun testAdd() {
        val q = PriorityQueue<Int>(SIZE)
        for (i in 0..SIZE - 1) {
            assertEquals(i, q.size)
            assertTrue(q.add(i))
        }
    }

    /**
     * addAll of a collection with null elements throws NPE
     */
    @Test
    fun testAddAll2() {
        try {
            val q = PriorityQueue<Int>(SIZE)
            val ints = arrayOfNulls<Int>(SIZE)
            q.addAll(Arrays.asList<Int>(*ints))
            shouldThrow()
        } catch (success: NullPointerException) {
        }
    }

    /**
     * addAll of a collection with any null elements throws NPE after
     * possibly adding some elements
     */
    @Test
    fun testAddAll3() {
        try {
            val q = PriorityQueue<Int>(SIZE)
            val ints = arrayOfNulls<Int>(SIZE)
            for (i in 0..SIZE - 1 - 1)
                ints[i] = i
            q.addAll(Arrays.asList<Int>(*ints))
            shouldThrow()
        } catch (success: NullPointerException) {
        }
    }

    /**
     * Queue contains all elements of successful addAll
     */
    @Test
    fun testAddAll5() {
        val empty = arrayOfNulls<Int>(0)
        val ints = arrayOfNulls<Int>(SIZE)
        for (i in 0..SIZE - 1) {
            ints[i] = SIZE - 1 - i
        }
        val q = PriorityQueue<Int>(SIZE)
        assertFalse(q.addAll(Arrays.asList<Int>(*empty)))
        assertTrue(q.addAll(Arrays.asList<Int>(*ints)))
        for (i in 0..SIZE - 1) {
            assertEquals(i, q.poll())
        }
    }

    /**
     * poll succeeds unless empty
     */
    @Test
    fun testPoll() {
        val q = populatedQueue(SIZE)
        for (i in 0..SIZE - 1) {
            assertEquals(i, (q.poll() as Int).toInt())
        }
        assertNull(q.poll())
    }

    /**
     * peek returns next element, or null if empty
     */
    @Test
    fun testPeek() {
        val q = populatedQueue(SIZE)
        for (i in 0..SIZE - 1) {
            assertEquals(i, (q.peek() as Int).toInt())
            q.poll()
            assertTrue(q.peek() == null || i != (q.peek() as Int).toInt())
        }
        assertNull(q.peek())
    }

    /**
     * element returns next element, or throws NSEE if empty
     */
    @Test
    fun testElement() {
        val q = populatedQueue(SIZE)
        for (i in 0..SIZE - 1) {
            assertEquals(i, (q.element() as Int).toInt())
            q.poll()
        }
        try {
            q.element()
            shouldThrow()
        } catch (success: NoSuchElementException) {
        }
    }

    /**
     * remove removes next element, or throws NSEE if empty
     */
    @Test
    fun testRemove() {
        val q = populatedQueue(SIZE)
        for (i in 0..SIZE - 1) {
            assertEquals(i, (q.remove() as Int).toInt())
        }
        try {
            q.remove()
            shouldThrow()
        } catch (success: NoSuchElementException) {
        }
    }

    /**
     * remove(x) removes x and returns true if present
     */
    @Test
    fun testRemoveElement() {
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

    /**
     * contains(x) reports true when elements added but not yet removed
     */
    @Test
    fun testContains() {
        val q = populatedQueue(SIZE)
        for (i in 0..SIZE - 1) {
            assertTrue(q.contains(i))
            q.poll()
            assertFalse(q.contains(i))
        }
    }

    /**
     * clear removes all elements
     */
    @Test
    fun testClear() {
        val q = populatedQueue(SIZE)
        q.clear()
        assertTrue(q.isEmpty())
        assertEquals(0, q.size)
        q.add(1)
        assertFalse(q.isEmpty())
        q.clear()
        assertTrue(q.isEmpty())
    }

    /**
     * containsAll(c) is true when c contains a subset of elements
     */
    @Test
    fun testContainsAll() {
        val q = populatedQueue(SIZE)
        val p = PriorityQueue<Int>(SIZE)
        for (i in 0..SIZE - 1) {
            assertTrue(q.containsAll(p))
            assertFalse(p.containsAll(q))
            p.add(i)
        }
        assertTrue(p.containsAll(q))
    }

    /**
     * retainAll(c) retains only those elements of c and reports true if changed
     */
    @Test
    fun testRetainAll() {
        val q = populatedQueue(SIZE)
        val p = populatedQueue(SIZE)
        for (i in 0..SIZE - 1) {
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

    /**
     * removeAll(c) removes only those elements of c and reports true if changed
     */
    @Test
    fun testRemoveAll() {
        for (i in 1..SIZE - 1) {
            val q = populatedQueue(SIZE)
            val p = populatedQueue(i)
            assertTrue(q.removeAll(p))
            assertEquals(SIZE - i, q.size)
            for (j in 0..i - 1) {
                assertFalse(q.contains(p.remove()))
            }
        }
    }

    /**
     * toArray contains all elements
     */
    @Test
    fun testToArray() {
        val q = populatedQueue(SIZE)
        val o = q.toTypedArray()
        Arrays.sort(o)
        for (i in o.indices)
            assertEquals(o[i], q.poll())
    }

    /**
     * toArray(a) contains all elements
     */
    @Test
    fun testToArray2() {
        val q = populatedQueue(SIZE)
        @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
        var ints = arrayOfNulls<Int>(SIZE)
        ints = q.toTypedArray() as Array<Int?>
        Arrays.sort(ints)
        for (i in ints.indices)
            assertEquals(ints[i], q.poll())
    }

    /**
     * iterator iterates through all elements
     */
    @Test
    fun testIterator() {
        val q = populatedQueue(SIZE)
        var i = 0
        val it = q.iterator()
        while (it.hasNext()) {
            assertTrue(q.contains(it.next()))
            ++i
        }
        assertEquals(i, SIZE)
    }

    /**
     * iterator.remove removes current element
     */
    @Test
    fun testIteratorRemove() {
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


    /**
     * toString contains toStrings of elements
     */
    @Test
    fun testToString() {
        val q = populatedQueue(SIZE)
        val s = q.toString()
        for (i in 0..SIZE - 1) {
            assertTrue(s.indexOf(i.toString()) >= 0)
        }
    }

    /**
     * A deserialized serialized queue has same elements
     */
    @Test
    fun testSerialization() {
        val q = populatedQueue(SIZE)
        try {
            val bout = ByteArrayOutputStream(10000)
            val output = ObjectOutputStream(BufferedOutputStream(bout))
            output.writeObject(q)
            output.close()

            val bin = ByteArrayInputStream(bout.toByteArray())
            val input = ObjectInputStream(BufferedInputStream(bin))
            val r = input.readObject() as PriorityQueue<*>
            assertEquals(q.size, r.size)
            while (!q.isEmpty()) {
                assertEquals(q.remove(), r.remove())
            }
        } catch (e: Exception) {
            println(e)
            shouldThrow()
        }
    }

    private fun shouldThrow() {
        assertTrue(false)
    }

    internal class MyReverseComparator : Comparator<Int> {

        override fun compare(x: Int, y: Int): Int {
            if (x < y) return 1
            if (x > y) return -1
            return 0
        }
    }
}
