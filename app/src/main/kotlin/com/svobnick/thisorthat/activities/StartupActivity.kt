package com.svobnick.thisorthat.activities

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.databinding.ActivityStartupBinding
import com.svobnick.thisorthat.presenters.StartupPresenter
import com.svobnick.thisorthat.view.StartupView
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class StartupActivity : MvpAppCompatActivity(), StartupView {
    private lateinit var binding: ActivityStartupBinding

    @InjectPresenter
    lateinit var sPresenter: StartupPresenter

    @ProvidePresenter
    fun provideStartupPresenter(): StartupPresenter {
        return StartupPresenter(application as ThisOrThatApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding = ActivityStartupBinding.inflate(layoutInflater)
        setContentView(binding.root)
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