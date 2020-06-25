package com.svobnick.thisorthat.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.moxy.MvpAppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.adapters.MainScreenViewPagerAdapter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.presenters.MainScreenPresenter
import com.svobnick.thisorthat.view.MainScreenView
import kotlinx.android.synthetic.main.activity_main_screen.*

class MainScreenActivity : MvpAppCompatActivity(), MainScreenView {
    private lateinit var adapter: MainScreenViewPagerAdapter
    lateinit var viewPager: ViewPager2

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var msPresenter: MainScreenPresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun providePresenter(): MainScreenPresenter {
        return MainScreenPresenter(application as ThisOrThatApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)
        msPresenter.attachView(this)
        setupUI(main_screen_root)

        MobileAds.initialize(this)
        MobileAds.setRequestConfiguration(RequestConfiguration.Builder().setTestDeviceIds(listOf("A933D6D3E36429812DB83020D06BEAC7")).build())

        adapter = MainScreenViewPagerAdapter(this)
        viewPager = main_fragments_view_pager
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false
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
            val inputMethodManager = ContextCompat.getSystemService(this, InputMethodManager::class.java)!!
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    override fun switchFragment(fragmentNumber: Int) {
        if (this::viewPager.isInitialized) {
            viewPager.setCurrentItem(fragmentNumber, false)
        }
    }
}