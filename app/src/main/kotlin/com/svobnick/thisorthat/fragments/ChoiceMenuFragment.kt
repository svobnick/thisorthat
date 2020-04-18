package com.svobnick.thisorthat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.moxy.MvpAppCompatFragment
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.activities.CommentsActivity
import com.svobnick.thisorthat.activities.MainScreenActivity
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

        comments_button.setOnClickListener { commentsHandler() }
        switch_favorite_button.setOnClickListener { addFavoriteHandler() }
        share_button.setOnClickListener { shareHandler() }
    }

    override fun commentsHandler() {
        val intent = Intent(context, CommentsActivity::class.java)
        startActivity(intent)
    }

    override fun addFavoriteHandler() {
        choiceFragment().addFavoriteQuestion()
        switch_favorite_button.setImageResource(R.drawable.icon_favorite)
    }

    override fun shareHandler() {
        choiceFragment().shareQuestion()
    }

    private fun parentActivity(): MainScreenActivity {
        return activity as MainScreenActivity
    }

    private fun choiceFragment(): ChoiceFragment {
        return parentActivity().supportFragmentManager.findFragmentByTag("f" + parentActivity().viewPager.currentItem) as ChoiceFragment
    }

}