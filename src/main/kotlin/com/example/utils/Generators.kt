package com.example.utils

import kotlin.random.Random

fun generateRandomData(): String {
    return (1..STRING_LENGTH)
        .map { Random.nextInt(0, charPool.size).let { charPool[it] } }
        .joinToString("")
}