package com.svobnick.thisorthat.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Question(
    @PrimaryKey var id: Long,
    @ColumnInfo(name = "left_text") var thisText: String,
    @ColumnInfo(name = "right_text") var thatText: String
)