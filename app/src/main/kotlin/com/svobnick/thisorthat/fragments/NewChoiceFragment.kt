package com.svobnick.thisorthat.fragments

import android.os.Bundle
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
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.presenters.NewChoicePresenter
import com.svobnick.thisorthat.utils.PopupUtils.dimBackground
import com.svobnick.thisorthat.utils.PopupUtils.setupErrorPopup
import com.svobnick.thisorthat.view.NewChoiceView
import kotlinx.android.synthetic.main.error_popup_view.view.*
import kotlinx.android.synthetic.main.fragment_new_choice.*
import kotlinx.android.synthetic.main.fragment_new_choice.view.*

class NewChoiceFragment : MvpAppCompatFragment(), NewChoiceView {

    private lateinit var errorWindow: PopupWindow
    private lateinit var mInterstitialAd: InterstitialAd

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var newQuestionPresenter: NewChoicePresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun provideNewQuestionPresenter(): NewChoicePresenter {
        return NewChoicePresenter(activity!!.application as ThisOrThatApp)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_new_choice, container, false)
        errorWindow = setupErrorPopup(context!!)
        view.send_button.setOnClickListener(this::onSendQuestionButtonClick)

        initialAdvertisingComponent()

        return view
    }

    private fun initialAdvertisingComponent() {
        mInterstitialAd = InterstitialAd(context)
        mInterstitialAd.adUnitId = getString(R.string.new_choice_interstitial_ad_id)

        val adRequestBuilder = AdRequest.Builder()
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.i("Ads", "Loaded!")
            }

            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
                newQuestionPresenter.send(
                    new_first_text.text.toString(),
                    new_last_text.text.toString()
                )
            }
        }
        mInterstitialAd.loadAd(adRequestBuilder.build())
    }

    override fun onSendQuestionButtonClick(selected: View) {
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        } else {
            newQuestionPresenter.send(new_first_text.text.toString(), new_last_text.text.toString())
        }
    }

    override fun onSuccessfullyAdded() {
        new_first_text.text.clear()
        new_last_text.text.clear()
    }

    override fun showError(errorMsg: String) {
        errorWindow.contentView.error_text.text = errorMsg
        errorWindow.showAtLocation(activity!!.findViewById(R.id.main_screen_root), Gravity.CENTER, 0, 0)
        dimBackground(activity!!, errorWindow.contentView.rootView)
    }
}