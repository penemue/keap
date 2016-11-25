package com.github.penemue.keap

import java.util.*

fun <T> Collection<T>.keapify(comparator: Comparator<in T>? = null) = PriorityQueue(this, comparator)

fun <T> PriorityQueue<T>.copyOf() = PriorityQueue(this)

open class PriorityQueue<T>(capacity: Int = 0, cmp: Comparator<in T>? = null) : AbstractQueue<T>() {

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
        c.forEach { queue[nextFree++] = it }
        heapify()
    }

    constructor(c: PriorityQueue<T>) : this(c.queue.size, c.cmp) {
        count = c.count
        nextFree = c.nextFree
        queue copyFrom c.queue
        heap copyFrom c.heap
    }

    override fun peek(): T? = if (count == 0) null else queue[heap[0]]

    override fun poll(): T? {
        val result = pollRaw()
        if (result != null) {
            compactIfNecessary()
        }
        return result
    }

    /**
     * Retrieves and removes the least element if any without [compaction][compactIfNecessary] of the queue.
     */
    fun pollRaw(): T? {
        if (count == 0) {
            return null
        }
        return removeAt(heap[0])
    }

    override fun offer(e: T): Boolean {
        val i = nextFree
        if (i == queue.size) {
            val oldQueue = queue
            allocHeap(i + 1)
            var j = 0
            oldQueue.forEach { if (it != null) queue[j++] = it }
            nextFree = j
            heapify()
        }
        queue[nextFree] = e
        siftUp(nextFree)
        ++nextFree
        ++count
        ++modCount
        return true
    }

    override val size: Int get() = count

    override fun iterator(): MutableIterator<T> {
        return object : MutableIterator<T> {

            var expectedModCount = modCount
            var cursor = advance(0)

            override fun remove() {
                assertConcurrentUse()
                removeAt(cursor)
                expectedModCount = modCount
            }

            override fun next(): T {
                assertConcurrentUse()
                val result = queue[cursor] ?: throw NullPointerException()
                cursor = advance(cursor + 1)
                return result
            }

            override fun hasNext() = cursor < queue.size

            private fun advance(i: Int): Int {
                var j = i
                while (j < queue.size && queue[j] === null) {
                    ++j
                }
                return j
            }

            private fun assertConcurrentUse() =
                    if (expectedModCount != modCount) throw ConcurrentModificationException() else {
                    }
        }
    }

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
        if (count < queue.size / 3) {
            val oldQueue = queue
            allocHeap(count)
            var j = 0
            for (k in 0 until nextFree) {
                val e = oldQueue[k]
                if (e != null) {
                    queue[j++] = e
                }
            }
            nextFree = j
            heapify()
        }
    }

    private fun removeAt(i: Int): T? {
        val result = queue[i]
        if (result != null) {
            queue[i] = null
            siftUpToRoot(i)
            --count
            ++modCount
        }
        return result
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
        if (i1 < 0) {
            return i2
        }
        if (i2 < 0) {
            return i1
        }
        if (i1 >= i2) {
            throw IllegalArgumentException()
        }
        val value1 = queue[i1]
        val value2 = queue[i2]
        if (value1 === null && value2 === null) {
            return -1
        }
        return if (value1 === null) i2 else if (value2 === null) i1 else if (cmp.compare(value2, value1) > 0) i1 else i2
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
                if (this < 0) {
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