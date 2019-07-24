# Keap

[![Build Status](https://img.shields.io/travis/penemue/keap.svg)](https://travis-ci.org/penemue/keap)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.penemue/keap/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Ccom.github.penemue.keap)
[![Maintainability](https://api.codeclimate.com/v1/badges/4161941d1fdf6859d61f/maintainability)](https://codeclimate.com/github/penemue/keap/maintainability)
[![Apache License 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Pure Kotlin](https://img.shields.io/badge/100%25-kotlin-orange.svg)](https://kotlinlang.org)

Keap is a heap data structure written in [Kotlin](http://kotlinlang.org) similar to
[binary heap](https://en.wikipedia.org/wiki/Binary_heap). It maintains separately the queue of elements and the
[tournament tree](http://www.geeksforgeeks.org/tournament-tree-and-binary-heap) atop the queue. 

Keap is stable, that is, it keeps initial order of equal elements.

It's faster than binary heap in terms of number of comparisons. For any kind of input (random or ordered) of size `n`,
the *heapify* procedure requires exactly `n - 1` comparisons. Binary heap reaches this number of comparisons
only for ordered input. For random input, keap compared to binary heap does approximately 1.9 times less comparisons
to *heapify*, 2.7 times less comparisons to *offer*, and 3.5 times less comparisons to *poll*.
Though, array-backed keap on the average consumes 1.5 times more memory than array-backed binary heap.

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
<td><i>Θ(<i>n</i>)</td>
<td>exactly <i>n</i></td>
<td>exactly <i>n</i></td>
</tr>
</table>

Here are two applications: keap-based `PriorityQueue` as a replacement of `java.util.PriorityQueue` and *Keapsort*
sorting algorithm. Both might be useful in two cases:

1. stability is a must;
1. comparing elements is rather heavyweight operation (e.g., it requires a database access).

## PriorityQueue
PriorityQueue is a keap-based stable replacement of `java.util.PriorityQueue`.

## Keapsort
Keapsort is a [Heapsort](https://en.wikipedia.org/wiki/Heapsort) with keap used instead of binary heap. Keapsort is a
comparison-based sorting algorithm with a worst-case [O](https://en.wikipedia.org/wiki/Big_O_notation)(*n* log *n*)
runtime. As keap is a stable priority queue, Keapsort is stable. Unlike Heapsort, Keapsort doesn't have an in-place
version.

Like Heapsort, Keapsort produces first output after [Θ](https://en.wikipedia.org/wiki/Big_O_notation)(*n*) comparisons.
For Keapsort, this Θ(n) estimate is just equal to `n - 1`. To sort random input completely, Keapsort does almost
two times less comparisons than Heapsort, nearly 1% less comparisons than
[Mergesort](https://en.wikipedia.org/wiki/Merge_sort) and 1-2% more comparisons than
[Timsort](https://en.wikipedia.org/wiki/Timsort).

Like Heapsort, Keapsort is not a modern CPU-friendly algorithm since it has poor data cache performance.

Like Heapsort, Keapsort doesn't seem to parallelize well.


## Benchmarks
[JMH Gradle Plugin](https://github.com/melix/jmh-gradle-plugin) is used to build and run benchmarks.
Benchmark results are obtained on a PC running under Windows 7 with Intel(R) Core(TM) i7-3770 3.4 GHz CPU
and 64-bit JRE build 1.8.0_112-b15 with the following java parameters: `-Xms1g -Xmx1g`. To get results in your
environment, run:

    ./gradlew clean jar jmh

For both `java.util.PriorityQueue` and keap-based PriorityQueue, there are two types of benchmarks:

 1. Benchmarks examining operations with random elements: *heapify* (building the queue from a collection), *offer*,
 *peek*, and *poll*. The queue elements are random strings of length ~30 characters with constant 10-characters prefix.
 Queue size is 10000.
 1. Benchmarks examining offering ordered elements: *offerIncreasing* (successively increasing elements) and
 *offerDecreasing* (successively decreasing elements). The queue elements are strings of length ~30
 characters with constant 10-characters prefix. Queue size is 10000.

Current results grouped for easy review are as follows. Building the queue from collections of random elements:
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
The scores above are numbers of operations per microsecond, for *heapify* - per millisecond. So the greater the score,
the better performance.

## Download
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.penemue/keap/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Ccom.github.penemue.keap)
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
    compile 'com.github.penemue:keap:0.3.0'
}
```

## Building from Source
[Gradle](http://www.gradle.org) is used to build, test, and run benchmarks. JDK 1.8 and [Kotlin](http://kotlinlang.org)
1.3.10 are required. To build the project, run:

    ./gradlew

## ToDo

- Compare Keapsort to [Quicksort](https://en.wikipedia.org/wiki/Quicksort).

- Looks like a tree-backed version of keap could be exposed as an immutable/persistent/lock-free heap data structure.
In addition, it could support heap merge operation in
[Θ](https://en.wikipedia.org/wiki/Big_O_notation)(*1*) time.
