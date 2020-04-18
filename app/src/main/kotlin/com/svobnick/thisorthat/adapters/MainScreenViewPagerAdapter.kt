package com.svobnick.thisorthat.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.svobnick.thisorthat.activities.MainScreenActivity
import com.svobnick.thisorthat.fragments.ChoiceFragment
import com.svobnick.thisorthat.fragments.NewChoiceFragment
import com.svobnick.thisorthat.fragments.ProfileFragment

class MainScreenViewPagerAdapter(activity: MainScreenActivity) : FragmentStateAdapter(activity) {
    private val choiceFragment = ChoiceFragment()
    private val newChoiceFragment = NewChoiceFragment()
    private val profileFragment = ProfileFragment()

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> choiceFragment
            1 -> newChoiceFragment
            2 -> profileFragment
            else -> throw IllegalStateException("There are only three positions allowed in main screen viewPager")
        }
    }
}