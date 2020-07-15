package com.svobnick.thisorthat.activities

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.moxy.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.presenters.StartupPresenter
import com.svobnick.thisorthat.view.StartupView

class StartupActivity : MvpAppCompatActivity(), StartupView {

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var sPresenter: StartupPresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun provideStartupPresenter(): StartupPresenter {
        return StartupPresenter(application as ThisOrThatApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_startup)
        sPresenter.attachView(this)
    }

    override fun onResume() {
        super.onResume()
        sPresenter.checkRegistration()
    }

    override fun onStartupEnd() {
        val intent = Intent(this, MainScreenActivity::class.java)
        intent.flags = intent.flags or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
    }
}