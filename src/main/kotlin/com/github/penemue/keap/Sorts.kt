package com.github.penemue.keap

import java.util.*

inline fun <reified T> Collection<T>.keapSorted(cmp: Comparator<in T>? = null): Iterable<T> {
    return SortedIterable(this, cmp)
}

inline fun <reified T> Collection<T>.keapSort(cmp: Comparator<in T>? = null): Array<T> {
    val result = arrayOfNulls<T>(size)
    keapSorted(cmp).forEachIndexed { i, it -> result[i] = it }
    @Suppress("UNCHECKED_CAST")
    return result as Array<T>
}

inline fun <reified T> Array<T>.keapSort(cmp: Comparator<in T>? = null): Array<T> {
    return asList().keapSort(cmp)
}