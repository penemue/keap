#Keap

[![GitHub license](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
![Repository Size](https://reposs.herokuapp.com/?path=penemue/keap)


Keap is a heap data structure similar to [binary heap](https://en.wikipedia.org/wiki/Binary_heap).
How does it compare to binary heap?

- It's faster in terms number of comparisons. For any kind of input (random or ordered) of size `n`,
the *heapify* procedure (building heap from scratch) requires exactly `n - 1` comparisons. Binary heap reaches
this value only for ordered input, whereas it requires approximately 90% more comparisons to heapify a random input.
- Keap is stable, that is, it ***keeps*** natural order of equal elements.
- Array-backed keap requires 2-3 times more memory than array-backed binary heap.

There are two applications: keap-based `PriorityQueue` as a replacement of `java.util.PriorityQueue` and stable
`keapSort` sorting algorithm.

##PriorityQueue

##KeapSort

##Building from Source
JDK 1.8 and [Kotlin](https://github.com/JetBrains/kotlin) 1.0.5 are required. [Gradle](http://www.gradle.org) is used to build the project:

    ./gradlew build

##ToDo