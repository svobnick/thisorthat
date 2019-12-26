package com.svobnick.thisorthat.service

import androidx.room.Database
import androidx.room.RoomDatabase
import com.svobnick.thisorthat.dao.AnswerDao
import com.svobnick.thisorthat.dao.ClaimDao
import com.svobnick.thisorthat.dao.QuestionDao
import com.svobnick.thisorthat.model.Answer
import com.svobnick.thisorthat.model.Claim
import com.svobnick.thisorthat.model.Question

@Database(entities = [Question::class, Answer::class, Claim::class], version = 1)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun questionDao() : QuestionDao
    abstract fun answerDao() : AnswerDao
    abstract fun claimDao() : ClaimDao
}