package com.svobnick.thisorthat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.moxy.MvpAppCompatFragment
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.activities.ChoiceActivity
import com.svobnick.thisorthat.activities.NewQuestionActivity
import com.svobnick.thisorthat.activities.ProfileActivity
import com.svobnick.thisorthat.view.BottomMenuView
import kotlinx.android.synthetic.main.fragment_bottom_menu.*

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
    }

    override fun questionsHandler() {
        val intent = Intent(context, ChoiceActivity::class.java)
        intent.flags = intent.flags or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    override fun addQuestionHandler() {
        val intent = Intent(context, NewQuestionActivity::class.java)
        intent.flags = intent.flags or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    override fun openSettingsHandler() {
        val intent = Intent(context, ProfileActivity::class.java)
        intent.flags = intent.flags or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }
}