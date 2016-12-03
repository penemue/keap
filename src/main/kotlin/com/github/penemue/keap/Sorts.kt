package com.github.penemue.keap

import java.util.*

inline fun <reified T> Collection<T>.keapSort(cmp: Comparator<in T>? = null): Array<T> {
    val keap = this.keapify(cmp)
    val result = arrayOfNulls<T>(size)
    repeat(size, { result[it] = keap.pollRaw() })
    @Suppress("UNCHECKED_CAST")
    return result as Array<T>
}