package com.svobnick.thisorthat.dao

import androidx.room.*
import com.svobnick.thisorthat.model.Question
import io.reactivex.Single

@Dao
interface QuestionDao {

    @Query("SELECT * FROM question WHERE choice IS 'n'")
    fun getUnansweredQuestions(): Single<List<Question>>

    @Query("SELECT * FROM question WHERE choice IS NOT null")
    fun getAnsweredQuestions(): Single<List<Question>>

    @Query("SELECT * FROM question WHERE id IN (:ids)")
    fun getQuestionsByIds(ids: List<Long>): Single<List<Question>>

    @Insert
    fun insertAll(questions: List<Question>)

    @Update
    fun saveUserChoice(question: Question)

    @Delete
    fun delete(question: Question)

    @Query("DELETE FROM question WHERE id IN (:ids)")
    fun deleteByIds(ids: List<Long>)
}