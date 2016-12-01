#Keap

[![Build Status](https://travis-ci.org/penemue/keap.png?branch=master)](https://travis-ci.org/penemue/keap)
[![GitHub license](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
![Repository Size](https://reposs.herokuapp.com/?path=penemue/keap)

Keap is a heap data structure similar to [binary heap](https://en.wikipedia.org/wiki/Binary_heap).

Keap is stable, that is, it ***keeps*** initial order of equal elements.

It's faster than binary heap in terms of number of comparisons. For any kind of input (random or ordered) of size `n`,
the *heapify* procedure requires exactly `n - 1` comparisons. Binary heap reaches this number of comparisons
only for ordered input. For random input, keap does approximately 90% less comparisons than binary heap
to *heapify*, to *offer* and to *poll*. Though, array-backed keap consumes 2-3 times more memory than array-backed
binary heap.

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

1. stability is needed;
1. comparing elements is rather heavyweight operation (e.g., it requires a database access).

##PriorityQueue

PriorityQueue is a keap-based stable replacement of `java.util.PriorityQueue`. 

##Keapsort

Keapsort is a [Heapsort](https://en.wikipedia.org/wiki/Heapsort) with keap used instead of binary heap. Keapsort is a
comparison-based sorting algorithm with a worst-case [O](https://en.wikipedia.org/wiki/Big_O_notation)(*n* log *n*)
runtime. As keap is a stable priority queue, Keapsort is stable. Unlike Heapsort, Keapsort doesn't have an in-place
version.

Like Heapsort, Keapsort produces first output after [Θ](https://en.wikipedia.org/wiki/Big_O_notation)(*n*) comparisons.
For Keapsort, this Θ(n) estimation is just equal to `n - 1`. To sort random input completely, Keapsort does almost
two times less comparisons than Heapsort, nearly 1% less comparisons than
[Mergesort](https://en.wikipedia.org/wiki/Merge_sort) and 1-2% more comparisons than
[Timsort](https://en.wikipedia.org/wiki/Timsort).

Like Heapsort, Keapsort is not a modern CPU-friendly algorithm since it has poor data cache performance.
    
Like Heapsort, Keapsort doesn't seem to parallelize well.

##Building from Source
JDK 1.8 and [Kotlin](https://github.com/JetBrains/kotlin) 1.0.5 are required. [Gradle](http://www.gradle.org)
is used to build the project:

    ./gradlew build

##ToDo

Looks like a tree-backed version of keap could be exposed as an immutable/persistent/lock-free heap data structure.
In addition, it could support heap merge operation in
[Θ](https://en.wikipedia.org/wiki/Big_O_notation)(*1*) time.