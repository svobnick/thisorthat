package com.svobnick.thisorthat.activities

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import androidx.moxy.MvpAppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.squareup.picasso.Picasso
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.adapters.CommentsAdapter
import com.svobnick.thisorthat.adapters.EndlessRecyclerViewScrollListener
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.model.Comment
import com.svobnick.thisorthat.presenters.CommentsPresenter
import com.svobnick.thisorthat.utils.PopupUtils.dimBackground
import com.svobnick.thisorthat.utils.PopupUtils.setupErrorPopup
import com.svobnick.thisorthat.view.CommentsView
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.popup_error_view.view.*
import kotlinx.android.synthetic.main.single_choice_in_comment_view.*
import javax.inject.Inject

class CommentsActivity : MvpAppCompatActivity(), CommentsView {


    @Inject
    lateinit var picasso: Picasso

    @InjectPresenter
    lateinit var cPresenter: CommentsPresenter

    private lateinit var errorWindow: PopupWindow
    private lateinit var adapter: CommentsAdapter

    @ProvidePresenter
    fun providePresenter(): CommentsPresenter {
        return CommentsPresenter(application as ThisOrThatApp)
    }

    private lateinit var commentsList: RecyclerView
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        cPresenter.attachView(this)

        adapter = CommentsAdapter(picasso)
        errorWindow = setupErrorPopup(applicationContext)

        commentsList = comments_list
        commentsList.setHasFixedSize(true)
        commentsList.setItemViewCacheSize(100)
        val linearLayoutManager = LinearLayoutManager(this)
        commentsList.layoutManager = linearLayoutManager
        adapter.setHasStableIds(true)
        commentsList.adapter = adapter
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                // todo intent.extras!!.get("id") instead of 4
                cPresenter.getComments(4, page * cPresenter.LIMIT)
            }
        }
        commentsList.addOnScrollListener(scrollListener)

        fillQuestionFragment()

        // todo intent.extras!!.get("id") instead of 4
        cPresenter.getComments(4, 0)
    }

    private fun fillQuestionFragment() {
        c_first_text.text = intent.extras!!.get("firstText") as String
        c_last_text.text = intent.extras!!.get("lastText") as String
        c_first_peoples_amount.text = intent.extras!!.get("firstRate") as String
        c_last_peoples_amount.text = intent.extras!!.get("lastRate") as String
        c_first_percent_value.text = intent.extras!!.get("firstPercent") as String
        c_last_percent_value.text = intent.extras!!.get("lastPercent") as String
    }

    override fun setComments(it: List<Comment>) {
        adapter.setComments(it)
    }

    // todo add swipe-to-refresh to update the list
    override fun updateComments() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCommentAdded() {
        new_comment.text.clear()
    }

    override fun addComment(sendView: View) {
        cPresenter.addComment(new_comment.text.toString())
    }

    override fun showError(errorMsg: String) {
        errorWindow.contentView.error_text.text = errorMsg
        errorWindow.showAtLocation(comments_root, Gravity.CENTER, 0, 0)
        dimBackground(this, errorWindow.contentView.rootView)
    }
}