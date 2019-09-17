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

    // todo "skipped"
    /**
     * for 'first_text' – first
     * for 'last_text' – 'last'
     * for reported questions – 'skip'
     * */
    @ColumnInfo(name = "choice") var choice: String?
) {

    companion object {
        private val EMPTY = Question(-1, "", "", 0, 0, "skip")
        fun empty() = EMPTY

        val SKIPPED = "skip"
        val FIRST = "first"
        val LAST = "last"
    }
}