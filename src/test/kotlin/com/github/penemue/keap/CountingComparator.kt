package com.github.penemue.keap

import java.util.*

/**
 * [Comparator][java.util.Comparator] that counts number of invocations of [compare].
 */
internal class CountingComparator<T>(val comparator: Comparator<T>) : Comparator<T> {

    internal var count = 0

    override fun compare(o1: T, o2: T): Int {
        ++count
        return comparator.compare(o1, o2)
    }
}