package com.svobnick.thisorthat.activities

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.moxy.MvpAppCompatActivity
import com.android.volley.RequestQueue
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.presenters.StartupPresenter
import com.svobnick.thisorthat.view.StartupView
import javax.inject.Inject

class StartupActivity : MvpAppCompatActivity(), StartupView {

    @Inject
    lateinit var requestQueue: RequestQueue

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var startupPresenter: StartupPresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun provideStartupPresenter(): StartupPresenter {
        return StartupPresenter(application as ThisOrThatApp, requestQueue)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
        startupPresenter.attachView(this)

        RegistrationAsyncTask(this).execute()
    }

    override fun startup() {
        startupPresenter.checkRegistration()
    }

    override fun onStartupEnd() {
        val intent = Intent(this, ChoiceActivity::class.java)
        startActivity(intent)
    }

    class RegistrationAsyncTask(private val activity: StartupActivity) : AsyncTask<Void, Void, Unit>() {
        override fun onPostExecute(result: Unit) {
            super.onPostExecute(result)
            activity.onStartupEnd()
        }

        override fun doInBackground(vararg p0: Void): Unit {
            activity.startup()
        }
    }
}