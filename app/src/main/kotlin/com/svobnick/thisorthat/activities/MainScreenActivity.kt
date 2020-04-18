package com.svobnick.thisorthat.activities

import android.os.Bundle
import androidx.moxy.MvpAppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
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
    lateinit var mainScreenPresenter: MainScreenPresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun provideMainScreenPresenter(): MainScreenPresenter {
        return MainScreenPresenter(application as ThisOrThatApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)
        mainScreenPresenter.attachView(this)

        adapter = MainScreenViewPagerAdapter(this)
        viewPager = main_fragments_view_pager
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false
    }

    override fun switchFragment(fragmentNumber: Int) {
        if (this::viewPager.isInitialized) {
            viewPager.setCurrentItem(fragmentNumber, false)
        }
    }
}