#Keap

[![Build Status](https://travis-ci.org/penemue/keap.png?branch=master)](https://travis-ci.org/penemue/keap)
[![Apache License 2.0](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
![Repository Size](https://reposs.herokuapp.com/?path=penemue/keap)
[![Pure Kotlin](https://img.shields.io/badge/100%25-kotlin-orange.svg)](https://kotlinlang.org)

Keap is a heap data structure written in [Kotlin](http://kotlinlang.org) similar to
[binary heap](https://en.wikipedia.org/wiki/Binary_heap). It maintains separately the queue of elements and the
[tournament tree](http://www.geeksforgeeks.org/tournament-tree-and-binary-heap) atop the queue. 

Keap is stable, that is, it keeps initial order of equal elements.

It's faster than binary heap in terms of number of comparisons. For any kind of input (random or ordered) of size `n`,
the *heapify* procedure requires exactly `n - 1` comparisons. Binary heap reaches this number of comparisons
only for ordered input. For random input, keap compared to binary heap does approximately 90% less comparisons
to *heapify*, 20% more comparisons to *offer*<sup><a href="#todo">[todo]</a></sup>, and more than 3 times less
comparisons to *poll*. Though, array-backed keap consumes 2-3 times more memory than array-backed binary heap.

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

##PriorityQueue
PriorityQueue is a keap-based stable replacement of `java.util.PriorityQueue`. 

##Keapsort
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

##Building from Source
[Gradle](http://www.gradle.org) is used to build, test, and run benchmarks. JDK 1.8 and [Kotlin](http://kotlinlang.org)
1.0.6 are required. To build the project, run:

    ./gradlew

##Benchmarks
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

Current results are as follows:
```
Benchmark                                   Mode  Cnt   Score   Error   Units
JavaQueueOrderedBenchmark.offerDecreasing  thrpt   20   5.506 ± 0.071  ops/us
JavaQueueOrderedBenchmark.offerIncreasing  thrpt   20  31.228 ± 0.363  ops/us
JavaQueueRandomBenchmark.heapify           thrpt   20   1.812 ± 0.041  ops/ms
JavaQueueRandomBenchmark.offer             thrpt   20   3.652 ± 0.075  ops/us
JavaQueueRandomBenchmark.peek              thrpt   20  81.031 ± 0.488  ops/us
JavaQueueRandomBenchmark.poll              thrpt   20   4.267 ± 0.074  ops/us
KeapQueueOrderedBenchmark.offerDecreasing  thrpt   20   8.318 ± 0.048  ops/us
KeapQueueOrderedBenchmark.offerIncreasing  thrpt   20   7.539 ± 0.099  ops/us
KeapQueueRandomBenchmark.heapify           thrpt   20   3.315 ± 0.019  ops/ms
KeapQueueRandomBenchmark.offer             thrpt   20   3.349 ± 0.077  ops/us
KeapQueueRandomBenchmark.peek              thrpt   20  82.927 ± 0.484  ops/us
KeapQueueRandomBenchmark.poll              thrpt   20  11.522 ± 0.264  ops/us
```
The scores above are numbers of operations per microsecond, for *heapify* - per millisecond. So the greater the score,
the better performance.

##ToDo

- <span id="todo">On increasing capacity, get rid of *heapify*, thus reducing number of comparisons on *offer*.</span>
 
- Compare Keapsort to [Quicksort](https://en.wikipedia.org/wiki/Quicksort). 

- Looks like a tree-backed version of keap could be exposed as an immutable/persistent/lock-free heap data structure.
In addition, it could support heap merge operation in
[Θ](https://en.wikipedia.org/wiki/Big_O_notation)(*1*) time.