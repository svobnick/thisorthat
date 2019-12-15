package com.svobnick.thisorthat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.moxy.MvpAppCompatFragment
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.activities.FavoriteQuestionsActivity
import com.svobnick.thisorthat.activities.NewQuestionActivity
import com.svobnick.thisorthat.view.BottomMenuView
import kotlinx.android.synthetic.main.fragment_bottom_menu.*
import kotlinx.android.synthetic.main.activity_choice.*

class BottomMenuFragment : MvpAppCompatFragment(), BottomMenuView {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_bottom_menu, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        questions_button.setOnClickListener { questionsHandler() }
        add_question_button.setOnClickListener { addQuestionHandler() }
        settings_button.setOnClickListener { openSettingsHandler() }

        val fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.hide(questions_menu_popup)
        fragmentTransaction.commit()
    }

    override fun questionsHandler() {
        val fragmentTransaction = fragmentManager!!.beginTransaction()
        if (questions_menu_popup.isHidden) {
            fragmentTransaction.show(questions_menu_popup)
        } else {
            fragmentTransaction.hide(questions_menu_popup)
        }
        fragmentTransaction.commit()
    }

    override fun addQuestionHandler() {
        val intent = Intent(context, NewQuestionActivity::class.java)
        startActivity(intent)
    }

    override fun openSettingsHandler() {
        // todo change to settings intent
        val intent = Intent(context, FavoriteQuestionsActivity::class.java)
        startActivity(intent)
    }
}