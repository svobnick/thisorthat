package com.svobnick.thisorthat.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.moxy.MvpAppCompatActivity
import com.android.volley.RequestQueue
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.presenters.NewQuestionPresenter
import com.svobnick.thisorthat.view.NewQuestionView
import kotlinx.android.synthetic.main.activity_new_question.*
import javax.inject.Inject

class NewQuestionActivity : MvpAppCompatActivity(), NewQuestionView {

    @Inject
    lateinit var requestQueue: RequestQueue

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var newQuestionPresenter: NewQuestionPresenter

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun provideNewQuestionPresenter(): NewQuestionPresenter {
        return NewQuestionPresenter(application as ThisOrThatApp, requestQueue)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_question)
        newQuestionPresenter.attachView(this)
    }

    override fun onSendQuestionButtonClick(selected: View) {
        newQuestionPresenter.send(new_this_text.text.toString(), new_that_text.text.toString())
        val intent = Intent(this, ChoiceActivity::class.java)
        startActivity(intent)
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
    }
}