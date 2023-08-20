package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.svobnick.thisorthat.activities.MainScreenActivity
import com.svobnick.thisorthat.databinding.ActivityHistoryChoiceBinding
import com.svobnick.thisorthat.databinding.ActivityMainScreenBinding
import com.svobnick.thisorthat.databinding.FragmentChoiceMenuBinding
import com.svobnick.thisorthat.view.ChoiceMenuView
import moxy.MvpAppCompatFragment

class ChoiceMenuFragment : MvpAppCompatFragment(), ChoiceMenuView {

    private lateinit var binding: FragmentChoiceMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChoiceMenuBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.commentsButton.setOnClickListener { commentsHandler() }
        binding.switchFavoriteButton.setOnClickListener { switchFavoriteHandler() }
        binding.shareButton.setOnClickListener { shareHandler() }
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

    private fun choiceFragment(): ChoiceFragment {
        if (requireActivity() is MainScreenActivity) {
            val currentItem =
                ActivityMainScreenBinding.inflate(layoutInflater).mainFragmentsViewPager.currentItem
            return requireActivity().supportFragmentManager.findFragmentByTag("f$currentItem") as ChoiceFragment
        } else {
            return requireActivity().supportFragmentManager.findFragmentById(
                ActivityHistoryChoiceBinding.inflate(layoutInflater).historyChoice.id
            ) as ChoiceFragment
        }
    }
}