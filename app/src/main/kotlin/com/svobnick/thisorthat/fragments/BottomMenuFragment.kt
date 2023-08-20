package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.activities.CommentsActivity
import com.svobnick.thisorthat.activities.HistoryChoiceActivity
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.databinding.FragmentBottomMenuBinding
import com.svobnick.thisorthat.presenters.BottomMenuPresenter
import com.svobnick.thisorthat.view.BottomMenuView
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter


class BottomMenuFragment : MvpAppCompatFragment(), BottomMenuView {

    @InjectPresenter
    lateinit var presenter: BottomMenuPresenter

    private var prevClickTime: Long = 0
    private lateinit var binding: FragmentBottomMenuBinding

    @ProvidePresenter
    fun provideBottomMenuPresenter(): BottomMenuPresenter {
        return BottomMenuPresenter(
            requireActivity().application as ThisOrThatApp
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomMenuBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.choiceButton.setOnClickListener { switchFragment(0) }
        binding.newChoiceButton.setOnClickListener { switchFragment(1) }
        binding.profileButton.setOnClickListener { switchFragment(2) }
    }

    fun switchFragment(menuNumber: Int) {
        val current = SystemClock.elapsedRealtime()
        if (current - prevClickTime > 100L) {
            goBackToMainScreen()
            presenter.switchFragment(menuNumber)
            updateUI(menuNumber)
            prevClickTime = SystemClock.elapsedRealtime()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI(presenter.menuState.currentMenuItem)
    }

    override fun updateUI(menuNumber: Int) {
        when (menuNumber) {
            0 -> {
                binding.choiceButton.setImageResource(R.drawable.icon_choice)
                binding.newChoiceButton.setImageResource(R.drawable.icon_new_choice_disabled)
                binding.profileButton.setImageResource(R.drawable.icon_profile_disabled)
            }
            1 -> {
                binding.choiceButton.setImageResource(R.drawable.icon_choice_disabled)
                binding.newChoiceButton.setImageResource(R.drawable.icon_new_choice)
                binding.profileButton.setImageResource(R.drawable.icon_profile_disabled)
            }
            2 -> {
                binding.choiceButton.setImageResource(R.drawable.icon_choice_disabled)
                binding.newChoiceButton.setImageResource(R.drawable.icon_new_choice_disabled)
                binding.profileButton.setImageResource(R.drawable.icon_profile)
            }
        }
    }

    private fun goBackToMainScreen() {
        if (activity is CommentsActivity) {
            requireActivity().onBackPressed()
        }
        if (activity is HistoryChoiceActivity) {
            requireActivity().onBackPressed()
        }
    }
}