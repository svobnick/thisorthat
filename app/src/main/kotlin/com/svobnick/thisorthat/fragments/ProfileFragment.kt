package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.moxy.MvpAppCompatFragment
import androidx.viewpager2.widget.ViewPager2
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.FirebaseAnalytics
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.adapters.ProfileViewPagerAdapter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.ProfilePresenter
import com.svobnick.thisorthat.view.ProfileView
import kotlinx.android.synthetic.main.fragment_profile.*
import javax.inject.Inject

class ProfileFragment : MvpAppCompatFragment(), ProfileView {
    private val ANALYTICS_SCREEN_NAME = "User questions and favorites"

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var adapter: ProfileViewPagerAdapter
    private lateinit var viewPager: ViewPager2

    private val tabs = listOf("Мои вопросы", "Избранное")

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var profilePresenter: ProfilePresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun provideProfilePresenter(): ProfilePresenter {
        return ProfilePresenter(activity!!.application as ThisOrThatApp)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity!!.application as ThisOrThatApp).injector.inject(this)
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        profilePresenter.attachView(this)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ProfileViewPagerAdapter(this, profilePresenter)
        viewPager = view_pager
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.adapter = adapter

        TabLayoutMediator(tab_layout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }

    override fun onStart() {
        super.onStart()
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, ANALYTICS_SCREEN_NAME)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS,ANALYTICS_SCREEN_NAME)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    override fun setQuestions(position: Int, questions: List<Question>) {
        if (position == 0) {
            adapter.addMyQuestions(questions)
        } else {
            adapter.addFavoriteQuestions(questions)
        }
    }

    override fun showEmptyList(position: Int) {
        adapter.showEmptyList(position)
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
    }
}