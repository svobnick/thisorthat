package com.svobnick.thisorthat.utils

import kotlin.math.roundToInt

fun computeQuestionsPercentage(firstRate: Int, lastRate: Int): Pair<Int, Int> {
    val sum = firstRate + lastRate
    val firstPercent = ((firstRate.toDouble() / sum) * 100).roundToInt()
    val lastPercent = ((lastRate.toDouble() / sum) * 100).roundToInt()
    return Pair(firstPercent, lastPercent)
}