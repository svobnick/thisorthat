package com.svobnick.thisorthat.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.PopupWindow
import android.widget.TextView
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
import com.svobnick.thisorthat.model.Question
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
    private lateinit var emptyCommentsText: TextView
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private var questionId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        cPresenter.attachView(this)

        adapter = CommentsAdapter(picasso)
        errorWindow = setupErrorPopup(applicationContext)

        val params = intent.extras!!
        questionId = params["id"] as Long

        commentsList = comments_list
        emptyCommentsText = empty_comments_list
        commentsList.setHasFixedSize(true)
        commentsList.setItemViewCacheSize(100)
        val linearLayoutManager = LinearLayoutManager(this)
        commentsList.layoutManager = linearLayoutManager
        adapter.setHasStableIds(true)
        commentsList.adapter = adapter
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                cPresenter.getComments(questionId, page * cPresenter.LIMIT)
            }
        }
        commentsList.addOnScrollListener(scrollListener)

        new_comment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.length < 4) {
                    send_comment.visibility = GONE
                } else {
                    send_comment.visibility = VISIBLE
                }
            }
        })

        fillQuestionFragment()
    }

    override fun onStart() {
        super.onStart()
        cPresenter.getComments(questionId, 0)
    }

    override fun onStop() {
        super.onStop()
        adapter.clear()
        scrollListener.resetState()
    }


    private fun fillQuestionFragment() {
        val params = intent.extras!!
        c_first_text.text = params.get("firstText") as String
        c_last_text.text = params.get("lastText") as String
        c_first_peoples_amount.text = params.get("firstRate") as String
        c_last_peoples_amount.text = params.get("lastRate") as String
        c_first_percent_value.text = params.get("firstPercent") as String
        c_last_percent_value.text = params.get("lastPercent") as String

        if (Question.Choices.NOT_ANSWERED == params.get("choice") as String) {
            c_first_stat.visibility = GONE
            c_last_stat.visibility = GONE
        }
    }

    override fun setComments(it: List<Comment>) {
        adapter.setComments(it)
    }

    // todo add swipe-to-refresh to update the list
    override fun updateComments() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCommentAdded(comment: Comment) {
        adapter.addComment(comment)
        new_comment.text.clear()
    }

    override fun addComment(sendView: View) {
        cPresenter.addComment(new_comment.text.toString(), questionId)
    }

    override fun showEmptyComments() {
        commentsList.visibility = GONE
        emptyCommentsText.visibility = VISIBLE
    }

    override fun showError(errorMsg: String) {
        errorWindow.contentView.error_text.text = errorMsg
        errorWindow.showAtLocation(comments_root, Gravity.CENTER, 0, 0)
        dimBackground(this, errorWindow.contentView.rootView)
    }
}