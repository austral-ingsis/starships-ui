package edu.austral.ingsis.starships.ui.util

fun <T> concatenate(vararg lists: List<T>): List<T> {
    val result: MutableList<T> = ArrayList()
    lists.forEach { list: List<T> -> result.addAll(list) }
    return result
}

fun <T> List<T>.tail() = drop(1)