package com.svobnick.thisorthat.model


data class Question(
    var id: Long,
    var firstText: String,
    var lastText: String,
    var firstRate: Int,
    var lastRate: Int,
    var status: String,
    var choice: String
) {

    object Choices {
        const val NOT_ANSWERED = "n" // not answered yet
        const val MY_QUESTION = "mq" // history choice from my questions list
        const val FAVORITE_QUESTION = "fq" // history choice from favorite questions list

        // these three 'answers' depends on API (other don't allowed)
        const val FIRST = "first"
        const val LAST = "last"
        const val SKIP = "skip" // reported questions
    }

    object Status {
        const val NEW = "new"
        const val APPROVED = "approved"
    }

    override fun toString(): String {
        return "$firstText или $lastText"
    }
}