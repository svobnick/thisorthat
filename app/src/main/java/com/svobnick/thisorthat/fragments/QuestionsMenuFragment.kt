package com.svobnick.thisorthat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.moxy.MvpAppCompatFragment
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.activities.ChoiceActivity
import com.svobnick.thisorthat.activities.FavoriteQuestionsActivity
import com.svobnick.thisorthat.activities.MyQuestionsActivity
import com.svobnick.thisorthat.view.QuestionsMenuView
import kotlinx.android.synthetic.main.questions_menu_popup.*

class QuestionsMenuFragment : MvpAppCompatFragment(), QuestionsMenuView {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.questions_menu_popup, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        all_questions_button.setOnClickListener { allQuestionsHandler() }
        favorite_questions_button.setOnClickListener { favoriteQuestionsHandler() }
        my_questions_button.setOnClickListener { myQuestionsHandler() }
    }


    override fun allQuestionsHandler() {
        val intent = Intent(context, ChoiceActivity::class.java)
        startActivity(intent)
    }

    override fun favoriteQuestionsHandler() {
        val intent = Intent(context, FavoriteQuestionsActivity::class.java)
        startActivity(intent)
    }

    override fun myQuestionsHandler() {
        val intent = Intent(context, MyQuestionsActivity::class.java)
        startActivity(intent)
    }


}