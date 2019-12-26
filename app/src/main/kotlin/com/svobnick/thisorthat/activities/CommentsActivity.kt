package com.svobnick.thisorthat.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.moxy.MvpAppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.squareup.picasso.Picasso
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.adapters.CommentsAdapter
import com.svobnick.thisorthat.adapters.EndlessRecyclerViewScrollListener
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.model.Comment
import com.svobnick.thisorthat.presenters.CommentsPresenter
import com.svobnick.thisorthat.view.CommentsView
import kotlinx.android.synthetic.main.activity_comments.*
import javax.inject.Inject

class CommentsActivity : MvpAppCompatActivity(), CommentsView {

    @Inject
    lateinit var requestQueue: RequestQueue
    @Inject
    lateinit var picasso: Picasso

    @InjectPresenter
    lateinit var presenter: CommentsPresenter

    private lateinit var adapter: CommentsAdapter

    @ProvidePresenter
    fun provideCommentsPresenter(): CommentsPresenter {
        return CommentsPresenter(application as ThisOrThatApp, requestQueue)
    }

    private lateinit var commentsList: RecyclerView
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        presenter.attachView(this)

        adapter = CommentsAdapter(picasso)

        commentsList = comments_list
        commentsList.setHasFixedSize(true)
        commentsList.setItemViewCacheSize(100)
        val linearLayoutManager = LinearLayoutManager(this)
        commentsList.layoutManager = linearLayoutManager
        adapter.setHasStableIds(true)
        commentsList.adapter = adapter
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                presenter.getComments(1, page * presenter.LIMIT)
            }
        }
        commentsList.addOnScrollListener(scrollListener)

        presenter.getComments(1, 0)
    }

    override fun setComments(it: List<Comment>) {
        adapter.setComments(it)
    }

    // todo add swipe-to-refresh to update the list
    override fun updateComments() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addComment(sendView: View) {
        presenter.addComment(new_comment.text.toString())
    }

    override fun showError(errorMsg: String) {
        Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
    }
}