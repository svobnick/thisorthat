package com.svobnick.thisorthat.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Question(
    @PrimaryKey var id: Long,
    @ColumnInfo(name = "first_text") var firstText: String,
    @ColumnInfo(name = "second_text") var secondText: String,
    @ColumnInfo(name = "first_rate") var firstRate: Int,
    @ColumnInfo(name = "second_rate") var secondRate: Int,

    // todo "skipped"
    /**
     * true is 'first_text'
     * false is 'second_text'
     * null is unanswered question
     * */
    @ColumnInfo(name = "user_choice") var userChoice: Boolean?
) {

    companion object {
        private val EMPTY = Question(-1, "", "", 0, 0, null)
        fun empty() = EMPTY
    }
}