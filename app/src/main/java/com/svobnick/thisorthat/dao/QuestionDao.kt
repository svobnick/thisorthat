package com.svobnick.thisorthat.dao

import androidx.room.*
import com.svobnick.thisorthat.model.Question
import io.reactivex.Single

@Dao
interface QuestionDao {

    @Query("SELECT * FROM question WHERE user_choice IS null")
    fun getUnansweredQuestions(): Single<List<Question>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(questions: List<Question>)

    @Update
    fun saveUserChoice(question: Question)

    @Delete
    fun delete(question: Question)
}