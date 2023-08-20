package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.FirebaseAnalytics
import com.svobnick.thisorthat.adapters.ProfileViewPagerAdapter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.databinding.FragmentProfileBinding
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.ProfilePresenter
import com.svobnick.thisorthat.view.ProfileView
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

class ProfileFragment : MvpAppCompatFragment(), ProfileView {
    private val ANALYTICS_SCREEN_NAME = "User questions and favorites"

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var adapter: ProfileViewPagerAdapter

    private val tabs = listOf("Мои вопросы", "Избранное")

    @InjectPresenter
    lateinit var profilePresenter: ProfilePresenter

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @ProvidePresenter
    fun provideProfilePresenter(): ProfilePresenter {
        return ProfilePresenter(requireActivity().application as ThisOrThatApp)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity().application as ThisOrThatApp).injector.inject(this)
        _binding = FragmentProfileBinding.inflate(inflater,container,false)
        profilePresenter.attachView(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ProfileViewPagerAdapter(this, profilePresenter)
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}