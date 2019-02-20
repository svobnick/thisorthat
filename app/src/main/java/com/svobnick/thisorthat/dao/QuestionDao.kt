package com.svobnick.thisorthat.dao

import androidx.room.*
import com.svobnick.thisorthat.model.Question

@Dao
interface QuestionDao {

    @Query("SELECT * FROM question")
    fun getAll(): List<Question>

    @Query("SELECT * FROM question WHERE user_choice IS null")
    fun getUnansweredQuestions(): List<Question>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg questions: Question)

    @Update
    fun saveUserChoice(question: Question)

    @Delete
    fun delete(question: Question)
}