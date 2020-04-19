package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.moxy.MvpAppCompatFragment
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.activities.HistoryChoiceActivity
import com.svobnick.thisorthat.activities.MainScreenActivity
import com.svobnick.thisorthat.view.ChoiceMenuView
import kotlinx.android.synthetic.main.activity_history_choice.*
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
        switch_favorite_button.setOnClickListener { switchFavoriteHandler() }
        share_button.setOnClickListener { shareHandler() }
    }

    override fun commentsHandler() {
        choiceFragment().openComments()
    }

    override fun switchFavoriteHandler() {
        choiceFragment().switchFavoriteQuestion()
    }

    override fun shareHandler() {
        choiceFragment().shareQuestion()
    }

    private fun parentActivity(): FragmentActivity {
        return activity!!
    }

    private fun choiceFragment(): ChoiceFragment {
        if (parentActivity() is MainScreenActivity) {
            return parentActivity().supportFragmentManager.findFragmentByTag("f" + (parentActivity() as MainScreenActivity).viewPager.currentItem) as ChoiceFragment
        } else {
            return parentActivity().supportFragmentManager.findFragmentById((parentActivity() as HistoryChoiceActivity).history_choice.id) as ChoiceFragment
        }
    }

}