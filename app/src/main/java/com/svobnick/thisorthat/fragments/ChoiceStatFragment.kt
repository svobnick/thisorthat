package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.moxy.MvpAppCompatFragment
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.view.ChoiceStatView
import kotlinx.android.synthetic.main.fragment_choice_stat.*


class ChoiceStatFragment : MvpAppCompatFragment(), ChoiceStatView {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_choice_stat, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
    }

    override fun setStat(percentage: Int, amount: Int, userChoice: Boolean) {
        percent_value.text = percentage.toString()
        peoples_amount.text = amount.toString()
        if (!userChoice) {
            percent_value.alpha = 0.25f
            percent_symbol.alpha = 0.25f
        } else {
            percent_value.alpha = 1f
            percent_symbol.alpha = 1f
        }
    }
}