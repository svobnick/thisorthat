package com.svobnick.thisorthat.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Answer(
    @PrimaryKey var id: Long,
    // might be "first" / "last" / "skip"
    var choice: String
)