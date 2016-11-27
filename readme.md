#Keap

[![Build Status](https://travis-ci.org/penemue/keap.png?branch=master)](https://travis-ci.org/penemue/keap)
[![GitHub license](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
![Repository Size](https://reposs.herokuapp.com/?path=penemue/keap)

Keap is a heap data structure similar to [binary heap](https://en.wikipedia.org/wiki/Binary_heap).

- Keap is stable, that is, it ***keeps*** initial order of equal elements.
- It's faster than binary heap in terms of number of comparisons. For any kind of input (random or ordered) of size `n`,
the *heapify* procedure requires exactly `n - 1` comparisons. Binary heap reaches this number of comparisons
only for ordered input. For random input, keap does approximately 90% less comparisons than binary heap
to *heapify*, to *offer* and to *poll*.
- Array-backed keap requires 2-3 times more memory than array-backed binary heap.

Here are two applications: keap-based `PriorityQueue` as a replacement of `java.util.PriorityQueue` and `Keapsort`
sorting algorithm. Both might be useful in two cases:

1. Stability is required.
1. Comparing elements is rather heavyweight operation (e.g., it requires a database access).

##PriorityQueue

PriorityQueue is a keap-based replacement of `java.util.PriorityQueue`. It's stable. That's it. 

##Keapsort

Keapsort is a [Heapsort](https://en.wikipedia.org/wiki/Heapsort) with keap used instead of binary heap. Keapsort is
a comparison-based sorting algorithm with a worst-case [O](https://en.wikipedia.org/wiki/Big_O_notation)(n log n) runtime.
As keap is a stable priority queue, Keapsort is stable. Unlike Heapsort, Keapsort doesn't have an in-place version.

Like Heapsort, Keapsort produces first output after O(n) comparisons. For Keapsort, this O(n) estimation is just
equal to `n - 1`.

Like Heapsort, Keapsort is not modern CPU-friendly since it has poor data cache performance.
    
Like Heapsort, Keapsort doesn't seem to parallelize well.

To sort random input completely, Keapsort requires almost two times less comparisons than Heapsort,
nearly 1% less comparisons than [Mergesort](https://en.wikipedia.org/wiki/Merge_sort) and 1-2% more comparisons than
[Timsort](https://en.wikipedia.org/wiki/Timsort).

##Building from Source
JDK 1.8 and [Kotlin](https://github.com/JetBrains/kotlin) 1.0.5 are required. [Gradle](http://www.gradle.org) is used to build the project:

    ./gradlew build

##ToDo

Tree-based version of keap can easily be exposed as an immutable/persistent/lock-free heap data structure.
