package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.moxy.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.presenters.NewChoicePresenter
import com.svobnick.thisorthat.utils.PopupUtils.dimBackground
import com.svobnick.thisorthat.utils.PopupUtils.setupChoicePopup
import com.svobnick.thisorthat.utils.PopupUtils.setupErrorPopup
import com.svobnick.thisorthat.utils.PopupUtils.setupSuccessPopup
import com.svobnick.thisorthat.view.NewChoiceView
import kotlinx.android.synthetic.main.fragment_new_choice.*
import kotlinx.android.synthetic.main.fragment_new_choice.view.*
import kotlinx.android.synthetic.main.popup_choice_already_exist.view.*
import kotlinx.android.synthetic.main.popup_error_view.view.*
import javax.inject.Inject

class NewChoiceFragment : MvpAppCompatFragment(), NewChoiceView {
    private val ANALYTICS_SCREEN_NAME = "Question maker"

    private lateinit var mInterstitialAd: InterstitialAd
    private var prevClickTime: Long = 0

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var newChoicePresenter: NewChoicePresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun provideNewQuestionPresenter(): NewChoicePresenter {
        return NewChoicePresenter(activity!!.application as ThisOrThatApp)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity!!.application as ThisOrThatApp).injector.inject(this)

        val view = inflater.inflate(R.layout.fragment_new_choice, container, false)
        view.send_button.setOnClickListener(this::onSendQuestionButtonClick)

        initialAdvertisingComponent()

        return view
    }

    override fun onStart() {
        super.onStart()
        firebaseAnalytics.setCurrentScreen(this.activity!!, ANALYTICS_SCREEN_NAME, ANALYTICS_SCREEN_NAME)
    }

    private fun initialAdvertisingComponent() {
        mInterstitialAd = InterstitialAd(context)
        mInterstitialAd.adUnitId = getString(R.string.new_choice_interstitial_ad_id)

        val adRequestBuilder = AdRequest.Builder()
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdOpened() {
                firebaseAnalytics.logEvent("start_send_question_ad", null)
            }

            override fun onAdLoaded() {
                Log.i("Ads", "Loaded!")
            }

            override fun onAdClosed() {
                firebaseAnalytics.logEvent("send_question_ad_was_viewed", null)
                mInterstitialAd.loadAd(AdRequest.Builder().build())
                newChoicePresenter.send(
                    new_first_text.text.toString(),
                    new_last_text.text.toString()
                )
            }
        }
        mInterstitialAd.loadAd(adRequestBuilder.build())
    }

    override fun onSendQuestionButtonClick(selected: View) {
        val current = SystemClock.elapsedRealtime()
        if (current - prevClickTime > 500L) {
            firebaseAnalytics.logEvent("try_send_question", null)
            prevClickTime = SystemClock.elapsedRealtime()
            if (mInterstitialAd.isLoaded) {
                mInterstitialAd.show()
            } else {
                newChoicePresenter.send(
                    new_first_text.text.toString(),
                    new_last_text.text.toString()
                )
            }
        }
    }

    override fun onSuccessfullyAdded() {
        firebaseAnalytics.logEvent("question_was_sent", null)
        clearForm()
        val successPopup = setupSuccessPopup(context!!, PopupWindow.OnDismissListener {
            (activity!!.supportFragmentManager.findFragmentById(R.id.bottom_menu) as BottomMenuFragment).switchFragment(
                2
            )
        })
        successPopup.showAtLocation(
            activity!!.findViewById(R.id.main_screen_root),
            Gravity.CENTER,
            0,
            0
        )
        dimBackground(activity!!, successPopup.contentView.rootView)
    }

    private fun clearForm() {
        new_first_text.text.clear()
        new_last_text.text.clear()
    }

    override fun showError(errorMsg: String) {
        val errorPopup = setupErrorPopup(context!!)
        errorPopup.contentView.error_text.text = errorMsg
        errorPopup.showAtLocation(
            activity!!.findViewById(R.id.main_screen_root),
            Gravity.CENTER,
            0,
            0
        )
        dimBackground(activity!!, errorPopup.contentView.rootView)
    }

    override fun onChoiceAlreadyExist(cloneId: String) {
        val choicePopup = setupChoicePopup(context!!,
            PopupWindow.OnDismissListener { clearForm() })
        choicePopup.contentView.choice_not_ok.setOnClickListener {
            clearForm()
            choicePopup.dismiss()
        }
        choicePopup.contentView.choice_ok.setOnClickListener {
            newChoicePresenter.addFavoriteQuestion(cloneId)
            choicePopup.dismiss()
        }

        choicePopup.showAtLocation(
            activity!!.findViewById(R.id.main_screen_root),
            Gravity.CENTER,
            0,
            0
        )
        dimBackground(activity!!, choicePopup.contentView.rootView)
    }
}