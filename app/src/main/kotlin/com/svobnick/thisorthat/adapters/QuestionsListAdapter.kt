package com.svobnick.thisorthat.adapters

import com.svobnick.thisorthat.model.Question

interface QuestionsListAdapter {

    fun addQuestions(question: List<Question>)
}