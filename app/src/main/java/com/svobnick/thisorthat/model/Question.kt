package com.svobnick.thisorthat.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Question(
    @PrimaryKey var id: Long,
    @ColumnInfo(name = "this_text") var firstText: String,
    @ColumnInfo(name = "that_text") var second: String,

    // todo "skipped"
    /**
     * true is 'this_text'
     * false is 'that_text'
     * null is unanswered question
     * */
    @ColumnInfo(name = "user_choice") var userChoice: Boolean?
)