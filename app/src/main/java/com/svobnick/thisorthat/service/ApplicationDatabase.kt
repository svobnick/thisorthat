package com.svobnick.thisorthat.service

import androidx.room.Database
import androidx.room.RoomDatabase
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Question

@Database(entities = arrayOf(Question::class), version = 1)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun questionDao() : QuestionDao
}