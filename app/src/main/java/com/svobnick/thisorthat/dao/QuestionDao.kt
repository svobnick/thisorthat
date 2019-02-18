package com.svobnick.thisorthat.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.svobnick.thisorthat.model.Question

@Dao
interface QuestionDao {

    @Query("SELECT * FROM question")
    fun getAll(): List<Question>

    @Insert
    fun insertAll(vararg questions: Question)

    @Delete
    fun delete(question: Question)
}