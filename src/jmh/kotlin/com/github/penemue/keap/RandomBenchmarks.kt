/**
 * Copyright 2016 - 2019 Vyacheslav Lukianov (https://github.com/penemue)
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

open class KeapQueueRandomBenchmark : RandomBenchmarkBase() {

    override fun createQueue(c: Collection<String>): Queue<String> {
        return PriorityQueue(c)
    }
}

open class JavaQueueRandomBenchmark : RandomBenchmarkBase() {

    override fun createQueue(c: Collection<String>): Queue<String> {
        return java.util.PriorityQueue(c)
    }
}

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
abstract class RandomBenchmarkBase {

    private companion object {
        private const val PRIORITY_QUEUE_SIZE = 10000
        private const val RANDOM_STRING_COUNT = 1000000
        private val randomStrings: Array<String>

        init {
            val r = Random()
            @Suppress("UNCHECKED_CAST")
            randomStrings = (arrayOfNulls<String>(RANDOM_STRING_COUNT) as Array<String>).apply {
                repeat(RANDOM_STRING_COUNT, {
                    this[it] = "0000000000${Math.abs(r.nextLong())}"
                })
            }
            shuffleStrings()
        }

        private fun shuffleStrings() {
            Collections.shuffle(randomStrings.asList())
        }
    }

    private var queue: Queue<String>? = null
    private var i = 0

    @Setup
    fun setupBenchmark() {
        shuffleStrings()
        queue = createQueue(randomStrings.copyOfRange(0, PRIORITY_QUEUE_SIZE).asList())
        i = PRIORITY_QUEUE_SIZE
    }

    @Setup(Level.Invocation)
    fun setupInvocation() {
        while (queue!!.size <= PRIORITY_QUEUE_SIZE) {
            offerRandomValue()
        }
        while (queue!!.size > PRIORITY_QUEUE_SIZE) {
            queue!!.poll()
        }
    }

    @Benchmark
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(4)
    fun heapify(bh: Blackhole) {
        val i = Math.min(this.i, RANDOM_STRING_COUNT - PRIORITY_QUEUE_SIZE)
        bh.consume(createQueue(randomStrings.copyOfRange(i, i + PRIORITY_QUEUE_SIZE).asList()))
    }

    @Benchmark
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(4)
    fun peek(bh: Blackhole) {
        bh.consume(queue!!.peek())
    }

    @Benchmark
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(4)
    fun poll(bh: Blackhole) {
        bh.consume(queue!!.poll())
    }

    @Benchmark
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    @Fork(4)
    fun offer(bh: Blackhole) {
        bh.consume(offerRandomValue())
    }

    private fun offerRandomValue(): Boolean {
        return queue!!.offer(randomStrings[i]).apply {
            if (++i == RANDOM_STRING_COUNT) i = 0
        }
    }

    abstract fun createQueue(c: Collection<String>): Queue<String>
}