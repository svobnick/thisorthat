package com.svobnick.thisorthat.model

data class Answer(
    var id: Long,
    // might be "first" / "last" / "skip"
    var choice: String
)