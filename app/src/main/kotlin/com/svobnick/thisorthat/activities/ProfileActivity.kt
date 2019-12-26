package com.svobnick.thisorthat.activities

import android.os.Bundle
import androidx.moxy.MvpAppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.adapters.ProfileViewPagerAdapter
import com.svobnick.thisorthat.view.ProfileActivityView
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : MvpAppCompatActivity(), ProfileActivityView {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        viewPager = tabWidget
        viewPager.adapter = ProfileViewPagerAdapter()

        TabLayoutMediator(
            tab_layout,
            viewPager,
            TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                tab.text = "Tab $position"
            }).attach()
    }

}