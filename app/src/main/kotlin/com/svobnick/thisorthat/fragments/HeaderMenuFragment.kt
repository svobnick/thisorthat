package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.svobnick.thisorthat.databinding.FragmentHeaderMenuBinding
import com.svobnick.thisorthat.view.HeaderMenuView
import moxy.MvpAppCompatFragment

class HeaderMenuFragment : MvpAppCompatFragment(), HeaderMenuView {

    private var _binding: FragmentHeaderMenuBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHeaderMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}