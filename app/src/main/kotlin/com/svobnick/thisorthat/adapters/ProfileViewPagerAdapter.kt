package com.svobnick.thisorthat.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.svobnick.thisorthat.fragments.FavoriteQuestionsListFragment
import com.svobnick.thisorthat.fragments.MyQuestionsListFragment
import com.svobnick.thisorthat.fragments.ProfileFragment
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.ProfilePresenter

class ProfileViewPagerAdapter(profileFragment: ProfileFragment, val presenter: ProfilePresenter) :
    FragmentStateAdapter(profileFragment) {
    private val myQuestionsFragment = MyQuestionsListFragment(presenter)
    private val favoriteQuestionsFragment = FavoriteQuestionsListFragment(presenter)

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