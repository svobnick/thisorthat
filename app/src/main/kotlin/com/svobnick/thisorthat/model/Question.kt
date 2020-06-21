package com.svobnick.thisorthat.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Question(
    @PrimaryKey var id: Long,
    @ColumnInfo(name = "first_text") var firstText: String,
    @ColumnInfo(name = "last_text") var lastText: String,
    @ColumnInfo(name = "first_rate") var firstRate: Int,
    @ColumnInfo(name = "last_rate") var lastRate: Int,
    @ColumnInfo(name = "status") var status: String,
    @ColumnInfo(name = "choice") var choice: String
) {

    object Choices {
        const val NOT_ANSWERED = "n" // not answered yet
        const val HISTORY = "h" // history choices from lists

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