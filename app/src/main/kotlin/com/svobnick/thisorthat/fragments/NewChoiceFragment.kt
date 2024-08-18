package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.google.firebase.analytics.FirebaseAnalytics
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.databinding.FragmentNewChoiceBinding
import com.svobnick.thisorthat.presenters.NewChoicePresenter
import com.svobnick.thisorthat.utils.PopupUtils.dimBackground
import com.svobnick.thisorthat.utils.PopupUtils.setupChoicePopup
import com.svobnick.thisorthat.utils.PopupUtils.setupErrorPopup
import com.svobnick.thisorthat.utils.PopupUtils.setupSuccessPopup
import com.svobnick.thisorthat.view.NewChoiceView
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

class NewChoiceFragment : MvpAppCompatFragment(), NewChoiceView {
    private val ANALYTICS_SCREEN_NAME = "Question maker"

    private var prevClickTime: Long = 0

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @InjectPresenter
    lateinit var newChoicePresenter: NewChoicePresenter

    private lateinit var binding: FragmentNewChoiceBinding

    @ProvidePresenter
    fun provideNewQuestionPresenter(): NewChoicePresenter {
        return NewChoicePresenter(requireActivity().application as ThisOrThatApp)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity().application as ThisOrThatApp).injector.inject(this)

        binding = FragmentNewChoiceBinding.inflate(inflater, container, false)
        binding.sendButton.setOnClickListener(this::onSendQuestionButtonClick)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, ANALYTICS_SCREEN_NAME)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, ANALYTICS_SCREEN_NAME)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

//    private fun initialAdvertisingComponent() {
//        mInterstitialAd = InterstitialAd(context)
//        mInterstitialAd.adUnitId = getString(R.string.new_choice_interstitial_ad_id)
//
//        val adRequestBuilder = AdRequest.Builder()
//        mInterstitialAd.adListener = object : AdListener() {
//            override fun onAdOpened() {
//                firebaseAnalytics.logEvent("start_send_question_ad", null)
//            }
//
//            override fun onAdLoaded() {
//                Log.i("Ads", "Loaded!")
//            }
//
//            override fun onAdClosed() {
//                firebaseAnalytics.logEvent("send_question_ad_was_viewed", null)
////                mInterstitialAd.loadAd(AdRequest.Builder().build())
//                newChoicePresenter.send(
//                    binding.newFirstText.text.toString(),
//                    binding.newLastText.text.toString()
//                )
//            }
//        }
//        mInterstitialAd.loadAd(adRequestBuilder.build())
//    }

    override fun onSendQuestionButtonClick(selected: View) {
        val current = SystemClock.elapsedRealtime()
        if (current - prevClickTime > 500L) {
            firebaseAnalytics.logEvent("try_send_question", null)
            prevClickTime = SystemClock.elapsedRealtime()
//            if (mInterstitialAd.isLoaded) {
//                mInterstitialAd.show()
//            } else {
            newChoicePresenter.send(
                binding.newFirstText.text.toString(),
                binding.newLastText.text.toString()
            )
//            }
        }
    }

    override fun onSuccessfullyAdded() {
        firebaseAnalytics.logEvent("question_was_sent", null)
        clearForm()
        val successPopup = setupSuccessPopup(requireContext(), PopupWindow.OnDismissListener {
            (requireActivity().supportFragmentManager.findFragmentById(R.id.main_bottom_menu) as BottomMenuFragment).switchFragment(
                2
            )
        })
        successPopup.first.showAtLocation(
            requireActivity().findViewById(R.id.main_screen_root),
            Gravity.CENTER,
            0,
            0
        )
        dimBackground(requireActivity(), successPopup.second.root)
    }

    private fun clearForm() {
        binding.newFirstText.text.clear()
        binding.newLastText.text.clear()
    }

    override fun showError(errorMsg: String) {
        val errorPopup = setupErrorPopup(requireContext())
        errorPopup.second.errorText.text = errorMsg
        errorPopup.first.showAtLocation(
            requireActivity().findViewById(R.id.main_screen_root),
            Gravity.CENTER,
            0,
            0
        )
        dimBackground(requireActivity(), errorPopup.second.root)
    }

    override fun onChoiceAlreadyExist(cloneId: String) {
        val choicePopup =
            setupChoicePopup(requireContext(), PopupWindow.OnDismissListener { clearForm() })
        choicePopup.second.choiceNotOk.setOnClickListener {
            clearForm()
            choicePopup.first.dismiss()
        }
        choicePopup.second.choiceOk.setOnClickListener {
            newChoicePresenter.addFavoriteQuestion(cloneId)
            choicePopup.first.dismiss()
        }

        choicePopup.first.showAtLocation(
            requireActivity().findViewById(R.id.main_screen_root),
            Gravity.CENTER,
            0,
            0
        )
        dimBackground(requireActivity(), choicePopup.second.root)
    }
}