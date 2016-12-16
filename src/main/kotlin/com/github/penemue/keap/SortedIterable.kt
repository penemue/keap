package com.github.penemue.keap

import java.util.*

open class SortedIterable<out T>(private val c: Collection<T>, private val cmp: Comparator<in T>? = null) : Iterable<T> {

    override fun iterator(): Iterator<T> {
        val keap = (c as? PriorityQueue)?.copyOf() ?: c.keapify(cmp)
        return object : Iterator<T> {

            override fun next(): T {
                return keap.pollRaw() ?: throw NullPointerException()
            }

            override fun hasNext(): Boolean {
                return keap.isNotEmpty()
            }
        }
    }

}