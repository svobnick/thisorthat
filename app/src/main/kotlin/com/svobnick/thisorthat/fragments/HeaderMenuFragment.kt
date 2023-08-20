package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.svobnick.thisorthat.databinding.FragmentHeaderMenuBinding
import com.svobnick.thisorthat.view.HeaderMenuView
import moxy.MvpAppCompatFragment

class HeaderMenuFragment : MvpAppCompatFragment(), HeaderMenuView {

    private lateinit var binding: FragmentHeaderMenuBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHeaderMenuBinding.inflate(inflater, container, false)
        return binding.root
    }
}