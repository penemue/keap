package com.github.penemue.keap

import java.lang.reflect.Method
import java.util.*
import java.util.PriorityQueue

/**
 * Inheritor of [java.util.PriorityQueue] with constructor applying [java.util.Comparator] and [java.util.Collection].
 */
class JavaPriorityQueue<T>(cmp: Comparator<in T>? = null, c: Collection<T>? = null) : PriorityQueue<T>() {

    init {
        val superclass = javaClass.superclass
        val field = superclass.getDeclaredField("comparator")
        field.isAccessible = true
        field.set(this, cmp)
        if (c != null) {
            var initMethod: Method? = null
            superclass.declaredMethods.filter { it.name == "initFromCollection" }.forEach {
                it.isAccessible = true
                initMethod = it
            }
            initMethod!!.invoke(this, c)
        }
    }
}