package com.svobnick.thisorthat.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.svobnick.thisorthat.databinding.FragmentChoiceStatBinding
import com.svobnick.thisorthat.view.ChoiceStatView
import moxy.MvpAppCompatFragment


class ChoiceStatFragment : MvpAppCompatFragment(), ChoiceStatView {

    private lateinit var binding: FragmentChoiceStatBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChoiceStatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setStat(percentage: Int, amount: Int, userChoice: Boolean) {
        val percentageAnim = ValueAnimator.ofInt(0, percentage)
        percentageAnim.duration = 500
        percentageAnim.addUpdateListener {
            binding.percentValue.text = it.animatedValue.toString()
        }

        val amountAnim = ValueAnimator.ofInt(0, amount)
        amountAnim.duration = 500
        amountAnim.addUpdateListener {
            binding.peoplesAmount.text = it.animatedValue.toString()
        }

        if (!userChoice) {
            binding.percentValue.alpha = 0.75f
            binding.percentSymbol.alpha = 0.75f
            binding.peoplesAmount.alpha = 0.75f
        } else {
            binding.percentValue.alpha = 1f
            binding.percentSymbol.alpha = 1f
            binding.peoplesAmount.alpha = 1f
        }

        binding.percentSymbol.visibility = VISIBLE
        percentageAnim.start()
        amountAnim.start()
    }
}