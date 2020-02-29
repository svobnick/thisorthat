package com.svobnick.thisorthat.activities

import android.os.Bundle
import android.widget.Toast
import androidx.moxy.MvpAppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.RequestQueue
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.android.material.tabs.TabLayoutMediator
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.adapters.ProfileViewPagerAdapter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.ProfilePresenter
import com.svobnick.thisorthat.view.ProfileView
import kotlinx.android.synthetic.main.activity_profile.*
import javax.inject.Inject

class ProfileActivity : MvpAppCompatActivity(), ProfileView {
    private lateinit var adapter: ProfileViewPagerAdapter
    private lateinit var viewPager: ViewPager2

    private val tabs = listOf("Мои вопросы", "Избранное")

    @Inject
    lateinit var requestQueue: RequestQueue

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var profilePresenter: ProfilePresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun provideProfilePresenter(): ProfilePresenter {
        return ProfilePresenter(application as ThisOrThatApp, requestQueue)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        profilePresenter.attachView(this)

        adapter = ProfileViewPagerAdapter(this, profilePresenter)
        viewPager = view_pager
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.adapter = adapter

        TabLayoutMediator(tab_layout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }

    override fun setQuestions(position: Int, questions: List<Question>) {
        if (position == 0) {
            adapter.addMyQuestions(questions)
        } else {
            adapter.addFavoriteQuestions(questions)
        }
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
    }
}