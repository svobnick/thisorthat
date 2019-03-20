package com.svobnick.thisorthat.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Answer(
    @PrimaryKey var id: Long,
    // might be 'left' or 'right'
    var userChoice: String
)