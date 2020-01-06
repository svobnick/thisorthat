package com.svobnick.thisorthat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.moxy.MvpAppCompatFragment
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.activities.ChoiceActivity
import com.svobnick.thisorthat.activities.CommentsActivity
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
        add_favorite_button.setOnClickListener { addFavoriteHandler() }
        share_button.setOnClickListener { shareHandler() }
    }

    override fun commentsHandler() {
        val intent = Intent(context, CommentsActivity::class.java)
        startActivity(intent)
    }

    override fun addFavoriteHandler() {
        (activity as ChoiceActivity).addFavoriteQuestion()
        add_favorite_button.setImageResource(R.drawable.icon_favorite)
    }

    override fun shareHandler() {

    }

}