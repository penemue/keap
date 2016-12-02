package com.github.penemue.keap

import java.util.*

fun <T> Collection<T>.keapify(comparator: Comparator<in T>? = null) = PriorityQueue(this, comparator)

fun <T> PriorityQueue<T>.copyOf() = PriorityQueue(this)

open class PriorityQueue<T>(capacity: Int = MIN_CAPACITY, cmp: Comparator<in T>? = null) : AbstractQueue<T>() {

    private var count = 0
    private var nextFree = 0
    private var modCount = 0
    @Suppress("UNCHECKED_CAST")
    private var queue: Array<T?> = arrayOfNulls<Any>(capacity.toCapacity) as Array<T?>
    private var heap = IntArray(queue.size - 1)
    @Suppress("UNCHECKED_CAST")
    private val cmp: Comparator<in T> = cmp ?: Comparator { o1, o2 -> (o1 as Comparable<T>).compareTo((o2)) }

    constructor(c: Collection<T>, comparator: Comparator<in T>? = null) : this(c.size, comparator) {
        count = c.size
        c.forEach { queue[nextFree++] = it ?: throw NullPointerException() }
        heapify()
    }

    constructor(c: PriorityQueue<T>) : this(c.queue.size, c.cmp) {
        count = c.count
        nextFree = c.nextFree
        queue copyFrom c.queue
        heap copyFrom c.heap
    }

    fun comparator() = cmp

    override fun peek(): T? = if (isEmpty()) null else queue[heap[0]]

    override fun poll(): T? {
        return pollRaw()?.apply { compactIfNecessary() }
    }

    /**
     * Retrieves and removes the least element if any without [compaction][compactIfNecessary] of the queue.
     * Is used in [keapSort].
     */
    fun pollRaw(): T? = if (isEmpty()) null else removeAt(heap[0])

    override fun offer(e: T): Boolean {
        val element = e ?: throw NullPointerException()
        val i = nextFree
        if (i == queue.size) {
            val oldQueue = queue
            allocHeap(i + 1)
            var j = 0
            oldQueue.forEach { it?.apply { queue[j++] = it } }
            nextFree = j
            heapify()
        }
        queue[nextFree] = element
        siftUp(nextFree)
        ++nextFree
        ++count
        ++modCount
        return true
    }

    override val size: Int get() = count

    override fun isEmpty() = count == 0

    override fun remove(element: T): Boolean {
        val i = indexOf(element)
        if (i < 0) return false
        removeAt(i)
        return true
    }

    override fun contains(element: T) = indexOf(element) >= 0

    override fun iterator(): MutableIterator<T> {
        return object : MutableIterator<T> {

            var expectedModCount = modCount
            var next: T? = null
            var cursor = -1

            override fun remove() {
                assertConcurrentUse()
                removeAt(cursor)
                expectedModCount = modCount
            }

            override fun next(): T {
                hasNext()
                return next?.apply { next = null } ?: throw NullPointerException()
            }

            override fun hasNext(): Boolean {
                assertConcurrentUse()
                var i = cursor
                while (next == null) {
                    if (++i == queue.size) break
                    next = queue[i]
                }
                cursor = i
                return next != null
            }

            private fun assertConcurrentUse() =
                    if (expectedModCount != modCount) throw ConcurrentModificationException() else {
                    }
        }
    }

    private fun indexOf(element: T) = queue.indexOfFirst { it != null && it == element }

    private fun heapify() {
        var i = heap.size
        if (i > 0) {
            i /= 2
            for (j in i..i * 2) {
                heap[j] = min(queueLeftChild(j), queueRightChild(j))
            }
            while (i > 0) {
                i /= 2
                for (j in i..i * 2) {
                    heap[j] = min(heap[j.leftChild], heap[j.rightChild])
                }
            }
        }
    }

    private fun siftUp(index: Int) {
        var i = queueParent(index)
        heap[i] = min(queueLeftChild(i), queueRightChild(i))
        while (i > 0) {
            i = i.parent
            val min = min(heap[i.leftChild], heap[i.rightChild])
            if (heap[i] == min) {
                break
            }
            heap[i] = min
        }
    }

    private fun siftUpToRoot(index: Int) {
        var i = queueParent(index)
        heap[i] = min(queueLeftChild(i), queueRightChild(i))
        while (i > 0) {
            i = i.parent
            heap[i] = min(heap[i.leftChild], heap[i.rightChild])
        }
    }

    private fun compactIfNecessary() {
        if (count < queue.size / 3 && count > 0) {
            val oldQueue = queue
            allocHeap(count)
            var j = 0
            repeat(nextFree, { oldQueue[it]?.apply { queue[j++] = this } })
            nextFree = j
            heapify()
        }
    }

    private fun removeAt(i: Int): T? {
        val result = queue[i]
        return result?.apply {
            queue[i] = null
            siftUpToRoot(i)
            --count
            ++modCount
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun allocHeap(capacity: Int) {
        queue = arrayOfNulls<Any?>(capacity.toCapacity) as Array<T?>
        heap = IntArray(queue.size - 1)
    }

    private fun queueLeftChild(parent: Int) = parent.leftChild - heap.size

    private fun queueRightChild(parent: Int) = parent.rightChild - heap.size

    private fun queueParent(child: Int) = (child + heap.size).parent

    private fun min(i1: Int, i2: Int): Int {
        if (i1 < 0) return i2
        if (i2 < 0) return i1
        if (i1 == i2) return i1

        val value1 = queue[i1]
        val value2 = queue[i2]
        if (value1 == null && value2 == null) return -1
        if (value1 == null) return i2
        if (value2 == null) return i1

        val compare = cmp.compare(value1, value2)
        if (compare == 0) return if (i1 < i2) i1 else i2
        if (compare < 0) return i1
        return i2
    }

    private companion object KeapMiscellaneous {

        private const val MIN_CAPACITY = 4
        private val powersOf2: IntArray

        init {
            powersOf2 = IntArray(31)
            var powerOf2 = 1
            for (i in powersOf2.indices) {
                powersOf2[i] = powerOf2
                powerOf2 *= 2
            }
        }

        private val Int.toCapacity: Int
            get() {
                if (this < 1) {
                    throw IllegalArgumentException()
                }
                // capacity if always a power of 2
                val i = Arrays.binarySearch(powersOf2, this)
                if (i < 0) {
                    return powersOf2[-i - 1]
                }
                return Math.max(powersOf2[i], MIN_CAPACITY)
            }

        private val Int.leftChild: Int get() = this * 2 + 1

        private val Int.rightChild: Int get() = this * 2 + 2

        private val Int.parent: Int get() = (this - 1) / 2

        private infix fun <E> Array<E>.copyFrom(src: Array<E>) = System.arraycopy(src, 0, this, 0, src.size)

        private infix fun IntArray.copyFrom(src: IntArray) = System.arraycopy(src, 0, this, 0, src.size)
    }
}