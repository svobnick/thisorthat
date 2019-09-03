package com.svobnick.thisorthat.activities

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.moxy.MvpAppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.adapters.CommentsAdapter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.model.Comment
import com.svobnick.thisorthat.presenters.CommentsPresenter
import com.svobnick.thisorthat.view.CommentsView
import javax.inject.Inject

class CommentsActivity : MvpAppCompatActivity(), CommentsView {
    private val adapter = CommentsAdapter()

    @Inject
    lateinit var requestQueue: RequestQueue

    @InjectPresenter
    lateinit var presenter: CommentsPresenter

    @ProvidePresenter
    fun provideCommentsPresenter(): CommentsPresenter {
        return CommentsPresenter(application as ThisOrThatApp, requestQueue)
    }

    lateinit var commentsList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        presenter.attachView(this)

        commentsList = findViewById(R.id.comments_list)
        commentsList.layoutManager = LinearLayoutManager(this)
        commentsList.adapter = adapter

        presenter.getComments(4)
    }

    override fun setComments(it: List<Comment>) {
        adapter.setComments(it)
    }

    // todo add swipe-to-refresh to update the list
    override fun updateComments() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addComment(sendView: View) {
        val view = findViewById<EditText>(R.id.edittext_comment)
        presenter.addComment(view.text.toString())
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
    }
}