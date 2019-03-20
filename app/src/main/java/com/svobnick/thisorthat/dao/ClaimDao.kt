package com.svobnick.thisorthat.dao

import androidx.room.*
import com.svobnick.thisorthat.model.Claim
import io.reactivex.Single

@Dao
interface ClaimDao {

    @Query("SELECT * FROM claim")
    fun getAllClaims(): Single<List<Claim>>

    @Insert
    fun saveClaim(claim: Claim)

    @Query("DELETE FROM claim")
    fun delete()
}