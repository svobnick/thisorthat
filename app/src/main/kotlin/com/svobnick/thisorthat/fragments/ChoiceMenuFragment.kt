package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.svobnick.thisorthat.activities.MainScreenActivity
import com.svobnick.thisorthat.databinding.ActivityHistoryChoiceBinding
import com.svobnick.thisorthat.databinding.ActivityMainScreenBinding
import com.svobnick.thisorthat.databinding.FragmentChoiceMenuBinding
import com.svobnick.thisorthat.view.ChoiceMenuView
import moxy.MvpAppCompatFragment

class ChoiceMenuFragment : MvpAppCompatFragment(), ChoiceMenuView {

    private var _binding: FragmentChoiceMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChoiceMenuBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
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

    private fun parentActivity(): FragmentActivity {
        return requireActivity()
    }

    private fun choiceFragment(): ChoiceFragment {
        if (parentActivity() is MainScreenActivity) {
            val currentItem =
                ActivityMainScreenBinding.inflate(layoutInflater).mainFragmentsViewPager.currentItem
            return parentActivity().supportFragmentManager.findFragmentByTag("f$currentItem") as ChoiceFragment
        } else {
            return parentActivity().supportFragmentManager.findFragmentById(
                ActivityHistoryChoiceBinding.inflate(layoutInflater).historyChoice.id
            ) as ChoiceFragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}