package com.svobnick.thisorthat.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Claim(
    @PrimaryKey var id: Long,
    @ColumnInfo(name = "claim_reason") val claimReason: String
)