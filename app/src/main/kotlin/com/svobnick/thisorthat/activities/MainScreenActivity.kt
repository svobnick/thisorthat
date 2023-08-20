package com.svobnick.thisorthat.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.svobnick.thisorthat.adapters.MainScreenViewPagerAdapter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.databinding.ActivityMainScreenBinding
import com.svobnick.thisorthat.presenters.MainScreenPresenter
import com.svobnick.thisorthat.view.MainScreenView
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class MainScreenActivity : MvpAppCompatActivity(), MainScreenView {
    private lateinit var adapter: MainScreenViewPagerAdapter
    private lateinit var binding: ActivityMainScreenBinding

    @InjectPresenter
    lateinit var msPresenter: MainScreenPresenter

    @ProvidePresenter
    fun providePresenter(): MainScreenPresenter {
        return MainScreenPresenter(application as ThisOrThatApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        msPresenter.attachView(this)
        setupUI(binding.mainScreenRoot)

        MobileAds.initialize(this)
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("A933D6D3E36429812DB83020D06BEAC7")).build()
        )

        adapter = MainScreenViewPagerAdapter(this)
        binding.mainFragmentsViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.mainFragmentsViewPager.adapter = adapter
        binding.mainFragmentsViewPager.isUserInputEnabled = false
    }

    private fun setupUI(view: View) {
        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                hideSoftKeyboard()
                view.performClick()
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView)
            }
        }
    }

    private fun hideSoftKeyboard() {
        currentFocus?.let {
            val inputMethodManager =
                ContextCompat.getSystemService(this, InputMethodManager::class.java)!!
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    override fun switchFragment(fragmentNumber: Int) {
        binding.mainFragmentsViewPager.setCurrentItem(fragmentNumber, false)
    }
}