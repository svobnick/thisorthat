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
import com.svobnick.thisorthat.view.MenuFirstLevelView
import kotlinx.android.synthetic.main.fragment_menu_first_level.*
import kotlinx.android.synthetic.main.fragment_bottom_menu.*

class MenuFirstLevelFragment : MvpAppCompatFragment(), MenuFirstLevelView {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_menu_first_level, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        questions_button.setOnClickListener { questionsHandler() }
        add_question_button.setOnClickListener { addQuestionHandler() }
        settings_button.setOnClickListener { openSettingsHandler() }

//        val fragmentTransaction = fragmentManager!!.beginTransaction()
//        fragmentTransaction.hide(second_level_menu)
//        fragmentTransaction.commit()
    }

    override fun questionsHandler() {
//        val fragmentTransaction = fragmentManager!!.beginTransaction()
//        if (second_level_menu.isHidden) {
//            fragmentTransaction.show(second_level_menu)
//        } else {
//            fragmentTransaction.hide(second_level_menu)
//        }
//        fragmentTransaction.commit()
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