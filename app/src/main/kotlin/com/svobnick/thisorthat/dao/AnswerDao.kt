package com.svobnick.thisorthat.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.svobnick.thisorthat.model.Answer
import io.reactivex.Single

@Dao
interface AnswerDao {

    @Query("SELECT * FROM answer")
    fun getAnswers(): Single<List<Answer>>

    @Insert
    fun saveAnswer(answer: Answer)

    @Query("DELETE FROM answer")
    fun clear()
}