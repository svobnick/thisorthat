package com.svobnick.thisorthat.utils

import kotlin.math.roundToInt

fun computeQuestionsPercentage(firstRate: Int, lastRate: Int): Pair<Int, Int> {
    val sum = firstRate + lastRate
    var firstPercent = 50
    var lastPercent = 50
    if (sum != 0) {
        firstPercent = ((firstRate.toDouble() / sum) * 100).roundToInt()
        lastPercent = 100 - firstPercent
    }
    return Pair(firstPercent, lastPercent)
}