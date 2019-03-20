package com.svobnick.thisorthat.dao

import androidx.room.*
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