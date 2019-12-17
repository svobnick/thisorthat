package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.moxy.MvpAppCompatFragment
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.view.ChoiceMenuView
import kotlinx.android.synthetic.main.fragment_choice_menu.*

class ChoiceMenuFragment : MvpAppCompatFragment(), ChoiceMenuView {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_choice_menu, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        comments_button.setOnClickListener { questionsHandler() }
//        add_favorite_button.setOnClickListener { addQuestionHandler() }
//        share_button.setOnClickListener { openSettingsHandler() }
    }

}