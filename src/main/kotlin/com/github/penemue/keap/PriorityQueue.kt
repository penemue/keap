/**
 * Copyright 2016 - 2023 Vyacheslav Lukianov (https://github.com/penemue)
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

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*
import kotlin.math.max

/**
 * Extension function creating the priority queue from an [iterable][Iterable] with optionally specified
 * [comparator][Comparator].
 */
fun <T> Iterable<T>.keapify(cmp: Comparator<in T>? = null): PriorityQueue<T> {
    if (this is Collection) {
        return PriorityQueue(this, cmp)
    }
    val result = PriorityQueue<T>()
    forEach { result.offer(it) }
    return result
}

/**
 * Extension function creating the copy of this priority queue.
 */
fun <T> PriorityQueue<T>.copyOf() = PriorityQueue(this)

/**
 * A priority [queue][java.util.Queue] based on keap, a heap data structure similar to binary heap.
 * It maintains separately the [queue][queue] of elements and the [tournament tree][heap] atop the queue.
 * The elements of the priority queue are ordered according to their [natural ordering][java.lang.Comparable],
 * or by a [comparator][Comparator] provided at queue construction time, depending on which constructor is
 * used. A priority queue does not permit `null` elements. A priority queue relying on natural ordering also
 * does not permit insertion of non-comparable objects (doing so results in `ClassCastException`).
 *
 * This priority queue is a full featured replacement of [java.util.PriorityQueue] with two advantages:
 * better performance and stability.
 *
 * Better performance means that the priority queue almost always does less comparisons of its elements.
 * For random input, the priority queue compared to `java.util.PriorityQueue` does approximately 90%
 * less comparisons to [heapify][heapify], 20% more comparisons to [offer][offer], and more than 3 times less
 * comparisons to [poll][poll]. Though, it requires 2-3 times more memory than `java.util.PriorityQueue`.
 *
 * Stability means that the priority queue keeps initial order of equal elements added to the queue.
 * This feature allows [Keapsort][keapSorted], a sorting algorithm similar to
 * [Heapsort][https://en.wikipedia.org/wiki/Heapsort], but stable and faster in terms of number of comparisons.
 *
 * This class and its iterator implement all of the *optional* methods of the [java.util.Collection] and
 * [java.util.Iterator] interfaces. The Iterator provided by [iterator][iterator] is *not*
 * guaranteed to traverse the elements of the priority queue in any order. If you need ordered
 * traversal, consider using [SortedIterable].
 *
 * The priority queue is not synchronized.
 *
 * @author Vyacheslav Lukianov (https://github.com/penemue)
 * @param T the type of elements held in this collection
 * @see SortedIterable
 * @see keapSorted
 * @see keapSort
 */
open class PriorityQueue<T>(
    capacity: Int = MIN_CAPACITY,
    private val cmp: Comparator<in T>? = null
) : AbstractQueue<T>(), Serializable {

    private var count = 0

    @Transient
    private var nextFree = 0

    @Transient
    private var modCount = 0

    @Suppress("UNCHECKED_CAST")
    @Transient
    private var queue: Array<T?> = arrayOfNulls<Any>(capacity.toCapacity) as Array<T?>

    @Transient
    private var heap = IntArray(queue.size / 2 - 1).apply { fill(NIL) }

    @Transient
    private var heapSize = heap.size

    /**
     * Creates a `PriorityQueue` with the default initial [capacity][MIN_CAPACITY] and whose elements
     * are ordered according to the specified comparator.
     *
     * @param  cmp the comparator that will be used to order this priority queue
     */
    constructor(cmp: Comparator<in T>) : this(MIN_CAPACITY, cmp)

    /**
     * Creates a `PriorityQueue` containing the elements in the specified collection ordered according
     * to the specified comparator. If the comparator is not set (i.e. it's `null`) the priority queue
     * will be ordered according to the [natural ordering][java.lang.Comparable] of its elements.
     *
     * @param  c the collection whose elements are to be placed into this priority queue
     * @param  cmp the comparator that will be used to order this priority queue
     */
    constructor(c: Collection<T>, cmp: Comparator<in T>? = null) : this(c.size, cmp) {
        count = c.size
        c.forEach { queue[nextFree++] = it ?: throw NullPointerException() }
        heapify()
    }

    /**
     * Copying constructor creates a `PriorityQueue` which is the same as the specified priority queue.
     *
     * @param c the priority queue which the copy should be created of
     * @see copyOf
     */
    constructor(c: PriorityQueue<T>) : this(c.queue.size, c.cmp) {
        count = c.count
        nextFree = c.nextFree
        queue copyFrom c.queue
        heap copyFrom c.heap
    }

    /**
     * Returns the comparator used to order the elements in this queue, or `null` if this queue is
     * sorted according to the {[natural ordering][java.lang.Comparable] of its elements.
     *
     * @return the comparator used to order this queue, or `null` if this queue is sorted according
     * to the natural ordering of its elements
     */
    fun comparator() = cmp

    override fun peek(): T? = if (isEmpty()) null else queue[heap[0]]

    override fun poll(): T? {
        return pollRaw()?.apply { compactIfNecessary() }
    }

    /**
     * Retrieves and removes the least element if any exists without [compaction][compactIfNecessary] of this queue.
     * Is used in [keapSort] in order to avoid unnecessary comparisons being performed during compaction.
     */
    fun pollRaw(): T? = if (isEmpty()) null else removeAtEven(heap[0])

    /**
     * Inserts the specified element into this priority queue.
     *
     * @return `true` (as specified by [Queue.offer()][java.util.Queue#offer])
     * @throws ClassCastException if the specified element cannot be compared with elements currently in this
     * priority queue according to the priority queue's ordering
     * @throws NullPointerException if the specified element is `null`
     */
    override fun offer(e: T?): Boolean {
        e ?: throw NullPointerException()
        val i = nextFree
        if (i.isOdd) {
            val neighbour = queue[i - 1] ?: throw NullPointerException()
            if (compareValues(e, neighbour) >= 0) {
                queue[i] = e
            } else {
                queue[i - 1] = e
                queue[i] = neighbour
                siftUp(i - 1)
            }
        } else if (i == queue.size) {
            val q = queue
            // do not allocate new array for the queue if there is enough free space (not less than ~25%)
            val newCapacity = max(i, ((count + 1) * 4 / 3).toCapacity)
            if (newCapacity > i) {
                allocHeap(newCapacity)
            }
            var j = 0
            if (q !== queue) {
                q.forEach { it?.apply { queue[j++] = this } }
            } else {
                q.forEach { it?.apply { q[j++] = this } }
                q.fill(null, j, i)
            }
            nextFree = j
            heapify()
            return offer(e)
        } else {
            queue[i] = e
            siftUp(i)
        }
        nextFree = i + 1
        ++count
        ++modCount
        return true
    }

    override val size: Int get() = count

    override fun isEmpty() = count == 0

    internal val capacity: Int get() = queue.size

    /**
     * Removes a single instance of the specified element from this queue, if it is present. More formally,
     * removes an element `e` such that `element.equals(e)`, if this queue contains one or more such elements.
     * Returns `true` if and only if this queue contained the specified `element` (or equivalently, if this
     * queue changed as a result of the call).
     *
     * @param element object to be removed from this queue, if present
     * @return `true` if this queue changed as a result of the call
     */
    override fun remove(element: T): Boolean {
        val i = indexOf(element)
        if (i < 0) return false
        removeAt(i)
        return true
    }

    /**
     * Returns `true` if this queue contains the specified `element`. More formally, returns `true`
     * if and only if this queue contains at least one element `e` such that `element.equals(e)`.
     *
     * @param element object to be checked for containment in this queue
     * @return `true` if this queue contains the specified element
     */
    override fun contains(element: T) = indexOf(element) >= 0

    /**
     * Returns an iterator over the elements in this queue. The iterator does not return the elements
     * in any particular order.
     *
     * @return an iterator over the elements in this queue
     * @see SortedIterable
     */
    override fun iterator(): MutableIterator<T> {
        return object : MutableIterator<T> {

            var expectedModCount = modCount
            var next: T? = null
            var cursor = -1

            override fun remove() {
                checkUnmodified()
                val i = cursor
                removeAt(i)
                if (i.isEven) {
                    --cursor
                }
                expectedModCount = modCount
            }

            override fun next(): T {
                hasNext()
                return next?.apply { next = null } ?: throw NullPointerException()
            }

            override fun hasNext(): Boolean {
                checkUnmodified()
                var i = cursor
                while (next == null) {
                    if (++i == queue.size) break
                    next = queue[i]
                }
                cursor = i
                return next != null
            }

            private fun checkUnmodified() {
                if (expectedModCount != modCount) throw ConcurrentModificationException()
            }
        }
    }

    private fun indexOf(element: T) = queue.indexOfFirst { it != null && it == element }

    private fun heapify() {
        heap.fill(NIL)
        var i = heapSize
        for (j in i / 2 until i) {
            val minLeft = queueLeftChild(j)
            if (min(minLeft, minLeft + 1) == minLeft + 1) {
                swapNeighboursAt(minLeft)
            }
            val minRight = queueRightChild(j)
            if (min(minRight, minRight + 1) == minRight + 1) {
                swapNeighboursAt(minRight)
            }
            val min = min(minLeft, minRight)
            if (min == NIL) break
            heap[j] = min
        }
        while (i > 1) {
            i /= 2
            for (j in i / 2 until i) {
                val min = min(heap[j.leftChild], heap[j.rightChild])
                if (min == NIL) break
                heap[j] = min
            }
        }
    }

    private fun siftUp(index: Int) {
        var i = queueParent(index)
        heap[i] = min(queueLeftChild(i), queueRightChild(i))
        while (i > 0) {
            i = i.parent
            val min = min(heap[i.leftChild], heap[i.rightChild])
            if (min == heap[i]) {
                if (min == index) continue
                break
            }
            heap[i] = min
        }
    }

    private fun compactIfNecessary() {
        if (count > 0 && count < queue.size / 3) {
            val oldQueue = queue
            allocHeap(count)
            var j = 0
            repeat(nextFree) { oldQueue[it]?.apply { queue[j++] = this } }
            nextFree = j
            heapify()
        }
    }

    private fun removeAt(i: Int): T? {
        if (i.isEven) return removeAtEven(i)
        val result = queue[i]
        return result?.apply {
            queue[i] = null
            if (i == nextFree - 1) {
                --nextFree
            }
            --count
            ++modCount
        }
    }

    // remove element at even index
    private fun removeAtEven(i: Int): T? {
        val result = queue[i]
        return result?.apply {
            queue[i] = queue[i + 1]
            queue[i + 1] = null
            siftUp(i)
            if (i == nextFree - 1) {
                var j = i
                while (j > 0 && queue[j - 1] == null) --j
                nextFree = j
            }
            --count
            ++modCount
        }
    }

    // even index is expected
    private fun swapNeighboursAt(i: Int): Int {
        val temp = queue[i]
        queue[i] = queue[i + 1]
        queue[i + 1] = temp
        return i
    }

    @Suppress("UNCHECKED_CAST")
    private fun allocHeap(capacity: Int) {
        val adjustedCapacity = capacity.toCapacity
        @Suppress("SENSELESS_COMPARISON") // queue can be null during deserialization
        if (queue != null && adjustedCapacity == queue.size) {
            throw IllegalArgumentException("Allocating keap of the same size as existing")
        }
        queue = arrayOfNulls<Any?>(adjustedCapacity) as Array<T?>
        heap = IntArray(queue.size / 2 - 1)
        heapSize = heap.size
    }

    private fun queueLeftChild(parent: Int) = (parent.leftChild - heapSize) * 2

    private fun queueRightChild(parent: Int) = (parent.rightChild - heapSize) * 2

    private fun queueParent(child: Int) = (child / 2 + heapSize).parent

    private fun min(i1: Int, i2: Int): Int {
        if (i2 == NIL) return i1
        if (i1 == NIL) return i2

        val value1 = queue[i1]
        val value2 = queue[i2]
        if (value1 == null && value2 == null) return NIL
        if (value2 == null) return i1
        if (value1 == null) return i2

        return if (compareValues(value1, value2) <= 0) i1 else i2
    }

    @Suppress("UNCHECKED_CAST")
    private fun compareValues(value1: T, value2: T) =
        cmp?.compare(value1, value2) ?: (value1 as Comparable<T>).compareTo(value2)

    private fun writeObject(output: ObjectOutputStream) {
        // write out element count
        output.defaultWriteObject()
        // write out all elements
        forEach { output.writeObject(it) }
    }

    private fun readObject(input: ObjectInputStream) {
        // read in element count
        input.defaultReadObject()
        // realloc keap
        allocHeap(count)
        // read in all elements
        repeat(count) {
            @Suppress("UNCHECKED_CAST")
            queue[it] = input.readObject() as T ?: throw NullPointerException()
        }
        // build the keap
        heapify()
        // the queue is compacted
        nextFree = count
    }

    internal companion object {

        private const val serialVersionUID = 2808197179219145169L

        private const val NIL = -1
        const val MIN_CAPACITY = 4

        private val Int.toCapacity: Int
            get() {
                if (this < 1) {
                    throw IllegalArgumentException()
                }
                // capacity if always a power of 2
                val result = Integer.highestOneBit(2 * this - 1)
                return if (result < MIN_CAPACITY) MIN_CAPACITY else result
            }

        private val Int.leftChild: Int get() = this * 2 + 1

        private val Int.rightChild: Int get() = this * 2 + 2

        private val Int.parent: Int get() = (this - 1) / 2

        private val Int.isOdd: Boolean get() = this.and(1) == 1

        private val Int.isEven: Boolean get() = this.and(1) == 0

        private infix fun <E> Array<E>.copyFrom(src: Array<E>) = System.arraycopy(src, 0, this, 0, src.size)

        private infix fun IntArray.copyFrom(src: IntArray) = System.arraycopy(src, 0, this, 0, src.size)
    }
}
