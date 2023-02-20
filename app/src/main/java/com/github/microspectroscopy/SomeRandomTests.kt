package com.github.microspectroscopy

fun main() {
//    test1()
//    test2()
    var x = 1L
    var y = x.inc()
    println(y)
}

fun test1() {
    val checkEx = "e^x"
    val vars = extractVariables(checkEx)
    println(vars)
}

fun test2() {
    val checkEx = "pi*r^2"
    val vars = extractVariables(checkEx)
    println(vars)
}
