package com.github.penemue.keap

import java.util.*

internal object RandomStrings {

    private val r = Random()

    fun randomStringListOfSize(size: Int): List<String> {
        return mutableListOf<String>().apply { repeat(size, { this.add(Math.abs(r.nextLong()).toString()) }) }
    }
}