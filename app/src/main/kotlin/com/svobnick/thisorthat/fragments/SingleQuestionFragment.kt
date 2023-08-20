package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.svobnick.thisorthat.databinding.SingleChoiceInCommentViewBinding
import moxy.MvpAppCompatFragment

class SingleQuestionFragment : MvpAppCompatFragment() {

    private lateinit var binding: SingleChoiceInCommentViewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SingleChoiceInCommentViewBinding.inflate(inflater)
        return binding.root
    }
}