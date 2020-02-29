package com.svobnick.thisorthat.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.svobnick.thisorthat.activities.ProfileActivity
import com.svobnick.thisorthat.fragments.QuestionsListFragment
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.ProfilePresenter

class ProfileViewPagerAdapter(profileActivity: ProfileActivity, val presenter: ProfilePresenter) :
    FragmentStateAdapter(profileActivity) {
    private val myQuestionsFragment = QuestionsListFragment(0, presenter)
    private val favoriteQuestionsFragment = QuestionsListFragment(1, presenter)

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> myQuestionsFragment
            1 -> favoriteQuestionsFragment
            else -> throw IllegalStateException("There are only two positions allowed in profile viewPager")
        }
    }

    fun addMyQuestions(questions: List<Question>) {
        myQuestionsFragment.addQuestionsToList(questions)
    }

    fun addFavoriteQuestions(questions: List<Question>) {
        favoriteQuestionsFragment.addQuestionsToList(questions)
    }
}