package com.svobnick.thisorthat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.moxy.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.presenters.NewChoicePresenter
import com.svobnick.thisorthat.view.NewChoiceView
import kotlinx.android.synthetic.main.fragment_new_choice.*
import kotlinx.android.synthetic.main.fragment_new_choice.view.*

class NewChoiceFragment() : MvpAppCompatFragment(), NewChoiceView {

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
        view.send_question.setOnClickListener(this::onSendQuestionButtonClick)
        return view
    }

    override fun onSendQuestionButtonClick(selected: View) {
        newQuestionPresenter.send(new_this_text.text.toString(), new_that_text.text.toString())
    }

    override fun showSuccess() {
        Toast.makeText(context, "Вопрос успешно добавлен", Toast.LENGTH_LONG).show()
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
    }
}