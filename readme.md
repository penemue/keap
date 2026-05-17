# Keap

[![Maven Central](https://img.shields.io/maven-central/v/com.github.penemue/keap.svg)](https://central.sonatype.com/artifact/com.github.penemue/keap)
[![Maintainability](https://api.codeclimate.com/v1/badges/4161941d1fdf6859d61f/maintainability)](https://codeclimate.com/github/penemue/keap/maintainability)
[![Apache License 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Pure Kotlin](https://img.shields.io/badge/100%25-kotlin-orange.svg)](https://kotlinlang.org)

Keap is a heap data structure written in [Kotlin](http://kotlinlang.org), similar to a
[binary heap](https://en.wikipedia.org/wiki/Binary_heap). It separately maintains the queue of elements and a
[tournament tree](http://www.geeksforgeeks.org/tournament-tree-and-binary-heap) atop the queue.

Keap is stable, that is, it keeps initial order of equal elements.

It is faster than binary heap in terms of the number of comparisons. For any kind of input (random or ordered) of
size `n`, the *heapify* procedure requires exactly `n - 1` comparisons. Binary heap reaches this number of
comparisons only for ordered input. For random input, compared to binary heap, keap does approximately 1.9 times
fewer comparisons to *heapify*, 2.7 times fewer to *offer*, and 3.5 times fewer to *poll*. However, an array-backed
keap on average consumes 1.5 times more memory than an array-backed binary heap.

Performance summary of keap and binary heap (both array-backed) is as follows:
<table>
<tr>
<th></th>
<th>Keap average</th>
<th>Keap worst case</th>
<th>Binary heap average</th>
<th>Binary heap worst case</th>
</tr>
<tr>
<td>Heapify</td>
<td>exactly <i>n - 1</i></td>
<td>exactly <i>n - 1</i></td>
<td><i>Θ</i>(<i>n</i>)</td>
<td><i>Θ</i>(<i>n</i>)</td>
</tr>
<tr>
<td>Peek</td>
<td><i>Θ</i>(1)</td>
<td><i>Θ</i>(1)</td>
<td><i>Θ</i>(1)</td>
<td><i>Θ</i>(1)</td>
</tr>
<tr>
<td>Poll</td>
<td><i>Θ</i>(log <i>n</i>)</td>
<td><i>Θ</i>(log <i>n</i>)</td>
<td><i>Θ</i>(log <i>n</i>)</td>
<td><i>Θ</i>(log <i>n</i>)</td>
</tr>
<tr>
<td>Offer</td>
<td><i>Θ</i>(1)</td>
<td><i>Θ</i>(log <i>n</i>)</td>
<td><i>Θ</i>(1)</td>
<td><i>Θ</i>(log <i>n</i>)</td>
</tr>
<tr>
<td>Memory used</td>
<td><i>Θ</i>(<i>n</i>)</td>
<td><i>Θ(<i>n</i>)</i></td>
<td>exactly <i>n</i></td>
<td>exactly <i>n</i></td>
</tr>
</table>

Here are two applications: a keap-based `PriorityQueue` as a replacement for `java.util.PriorityQueue` and the
*Keapsort* sorting algorithm. Both might be useful in two cases:

1. stability is a must;
1. comparing elements is a rather heavyweight operation (e.g., it requires a database access).

## PriorityQueue
PriorityQueue is a keap-based stable replacement of `java.util.PriorityQueue`.

## Keapsort
Keapsort is a [Heapsort](https://en.wikipedia.org/wiki/Heapsort) with a keap used instead of a binary heap. It is a
comparison-based sorting algorithm with a worst-case [O](https://en.wikipedia.org/wiki/Big_O_notation)(*n* log *n*)
runtime. Since keap is a stable priority queue, Keapsort is stable as well. Unlike Heapsort, Keapsort does not have
an in-place version.

Like Heapsort, Keapsort produces its first output after [Θ](https://en.wikipedia.org/wiki/Big_O_notation)(*n*)
comparisons. For Keapsort, this Θ(n) estimate is exactly `n - 1`. To sort random input completely, Keapsort does
almost half as many comparisons as Heapsort, roughly 1% fewer than
[Mergesort](https://en.wikipedia.org/wiki/Merge_sort), and 1–2% more than
[Timsort](https://en.wikipedia.org/wiki/Timsort).

Like Heapsort, Keapsort is not a modern-CPU-friendly algorithm because it has poor data-cache performance.

Like Heapsort, Keapsort does not seem to parallelize well.


## Benchmarks
The [JMH Gradle Plugin](https://github.com/melix/jmh-gradle-plugin) (plugin id `me.champeau.jmh`) is used to build
and run benchmarks. The benchmark scores below are historical, originally captured on a PC running Windows 7 with
an Intel(R) Core(TM) i7-3770 3.4 GHz CPU and 64-bit JRE 1.8.0_112-b15 with java parameters `-Xms1g -Xmx1g`.
To get results in your environment, run:

    ./gradlew clean jar jmh

For both `java.util.PriorityQueue` and the keap-based PriorityQueue, there are two types of benchmarks:

 1. Benchmarks measuring operations on random elements: *heapify* (building the queue from a collection), *offer*,
 *peek*, and *poll*. The queue elements are random strings about 30 characters long with a constant 10-character
 prefix. The queue size is 10000.
 1. Benchmarks measuring offering of ordered elements: *offerIncreasing* (successively increasing elements) and
 *offerDecreasing* (successively decreasing elements). The queue elements are strings about 30 characters long
 with a constant 10-character prefix. The queue size is 10000.

Results grouped for easy review are as follows. Building the queue from collections of random elements:
```
Benchmark                                   Mode  Cnt   Score   Error   Units
JavaQueueRandomBenchmark.heapify           thrpt   20   1.810 ± 0.039  ops/ms
KeapQueueRandomBenchmark.heapify           thrpt   20   3.487 ± 0.013  ops/ms
```
Basic queue operations with random elements:
```
Benchmark                                   Mode  Cnt   Score   Error   Units
JavaQueueRandomBenchmark.offer             thrpt   20   3.649 ± 0.041  ops/us
JavaQueueRandomBenchmark.peek              thrpt   20  79.458 ± 0.626  ops/us
JavaQueueRandomBenchmark.poll              thrpt   20   4.250 ± 0.070  ops/us
KeapQueueRandomBenchmark.offer             thrpt   20   3.727 ± 0.073  ops/us
KeapQueueRandomBenchmark.peek              thrpt   20  84.371 ± 0.499  ops/us
KeapQueueRandomBenchmark.poll              thrpt   20  11.993 ± 0.239  ops/us
```
Offering ordered elements:
```
Benchmark                                   Mode  Cnt   Score   Error   Units
JavaQueueOrderedBenchmark.offerDecreasing  thrpt   20   5.432 ± 0.177  ops/us
JavaQueueOrderedBenchmark.offerIncreasing  thrpt   20  30.795 ± 0.588  ops/us
KeapQueueOrderedBenchmark.offerDecreasing  thrpt   20   7.959 ± 0.033  ops/us
KeapQueueOrderedBenchmark.offerIncreasing  thrpt   20   8.860 ± 0.044  ops/us
```
The scores above are operations per microsecond, or per millisecond for *heapify*. Higher is better.

## Download
[![Maven Central](https://img.shields.io/maven-central/v/com.github.penemue/keap.svg)](https://central.sonatype.com/artifact/com.github.penemue/keap)
```xml
<!-- in Maven project -->
<dependency>
    <groupId>com.github.penemue</groupId>
    <artifactId>keap</artifactId>
    <version>0.3.0</version>
</dependency>
```
```groovy
// in Gradle project
dependencies {
    implementation 'com.github.penemue:keap:0.3.0'
}
```

## Building from Source
[Gradle](http://www.gradle.org) is used to build, test, and run benchmarks. JDK 1.8+ is required to run the produced
artifact; the project itself is built with [Kotlin](http://kotlinlang.org) 2.3.21. To build the project, run:


    ./gradlew

## ToDo

- Compare Keapsort to [Quicksort](https://en.wikipedia.org/wiki/Quicksort).

- A tree-backed version of keap could be exposed as an immutable/persistent/lock-free heap data structure.
In addition, it could support a heap-merge operation in
[Θ](https://en.wikipedia.org/wiki/Big_O_notation)(*1*) time.
