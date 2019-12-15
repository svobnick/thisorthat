package com.svobnick.thisorthat.utils

import java.lang.IllegalArgumentException
import kotlin.math.roundToInt

fun computeQuestionsPercentage(firstRate: Int, lastRate: Int): Pair<Int, Int> {
    val sum = firstRate + lastRate
    var firstPercent = 50
    var lastPercent = 50
    try {
        firstPercent = ((firstRate.toDouble() / sum) * 100).roundToInt()
    } catch (ignored: IllegalArgumentException) {
        // todo
    }
    try {
        lastPercent = ((lastRate.toDouble() / sum) * 100).roundToInt()
    } catch (ignored: IllegalArgumentException) {
        // todo
    }
    return Pair(firstPercent, lastPercent)
}