package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.svobnick.thisorthat.databinding.SingleChoiceInCommentViewBinding
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.model.SingleQuestionDataViewModel
import moxy.MvpAppCompatFragment

class SingleQuestionFragment : MvpAppCompatFragment() {

    lateinit var binding: SingleChoiceInCommentViewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SingleChoiceInCommentViewBinding.inflate(inflater)

        val viewModel = ViewModelProvider(requireActivity()).get(SingleQuestionDataViewModel::class.java)
        fillQuestionFragment(viewModel)

        return binding.root
    }

    private fun fillQuestionFragment(viewModel: SingleQuestionDataViewModel) {
        binding.cFirstText.text = viewModel.firstText.value
        binding.cLastText.text = viewModel.lastText.value
        binding.cFirstPeoplesAmount.text = viewModel.firstRate.value
        binding.cLastPeoplesAmount.text = viewModel.lastRate.value
        binding.cFirstPercentValue.text = viewModel.firstPercent.value
        binding.cLastPercentValue.text = viewModel.lastPercent.value

        if (Question.Choices.NOT_ANSWERED == viewModel.choice.value) {
            binding.cFirstStat.visibility = View.INVISIBLE
            binding.cLastStat.visibility = View.INVISIBLE
        }
    }

}