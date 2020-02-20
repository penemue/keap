/**
 * Copyright 2016 - 2020 Vyacheslav Lukianov (https://github.com/penemue)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("unused")

package com.github.penemue.keap

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.*
import java.util.concurrent.TimeUnit

open class KeapQueueOrderedBenchmark : OrderedBenchmarkBase() {

    override fun createQueue(): Queue<String> {
        return PriorityQueue()
    }
}

open class JavaQueueOrderedBenchmark : OrderedBenchmarkBase() {

    override fun createQueue(): Queue<String> {
        return java.util.PriorityQueue()
    }
}

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
abstract class OrderedBenchmarkBase {

    private companion object {
        private const val PRIORITY_QUEUE_SIZE = 10000
    }

    private var queue: Queue<String>? = null
    private var increasingCounter = 1000000000000000000L
    private var increasingElement = ""
    private var decreasingCounter = 1000000000000000000L
    private var decreasingElement = ""

    @Setup
    fun setupBenchmark() {
        queue = createQueue()
    }

    @Setup(Level.Invocation)
    fun setupInvocation() {
        while (queue!!.size > PRIORITY_QUEUE_SIZE) {
            queue!!.poll()
        }
        increasingElement = "0000000000${++increasingCounter}"
        decreasingElement = "0000000000${--decreasingCounter}"
    }

    @Benchmark
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(4)
    fun offerIncreasing(bh: Blackhole) {
        bh.consume(queue!!.offer(increasingElement))
    }

    @Benchmark
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(4)
    fun offerDecreasing(bh: Blackhole) {
        bh.consume(queue!!.offer(decreasingElement))
    }

    abstract fun createQueue(): Queue<String>
}