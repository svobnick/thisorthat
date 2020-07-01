package com.svobnick.thisorthat.activities

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.moxy.MvpAppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
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
    private val ANALYTICS_SCREEN_NAME = "Comments"

    @Inject
    lateinit var picasso: Picasso

    @InjectPresenter
    lateinit var cPresenter: CommentsPresenter

    private lateinit var errorWindow: PopupWindow
    private lateinit var adapter: CommentsAdapter
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @ProvidePresenter
    fun providePresenter(): CommentsPresenter {
        return CommentsPresenter(application as ThisOrThatApp)
    }

    private lateinit var commentsList: RecyclerView
    private lateinit var emptyCommentsText: TextView
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private var keyboardListenersAttached = false

    private var questionId: Long = 0
    private var prevClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        cPresenter.attachView(this)
        setupUI(comments_root)

        adapter = CommentsAdapter(picasso)
        errorWindow = setupErrorPopup(applicationContext)

        firebaseAnalytics = Firebase.analytics

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

        attachKeyboardListeners()

        fillQuestionFragment()
    }

    override fun onStart() {
        super.onStart()
        cPresenter.getComments(questionId, 0)
        firebaseAnalytics.setCurrentScreen(this, ANALYTICS_SCREEN_NAME, ANALYTICS_SCREEN_NAME)
    }

    override fun onStop() {
        super.onStop()
        adapter.clear()
        scrollListener.resetState()
    }

    private fun attachKeyboardListeners() {
        if (keyboardListenersAttached) {
            return
        }

        comments_root.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = comments_root.rootView.height - comments_root.height
            val contentViewTop = (window.decorView.bottom / 10)

            val broadcastManager = LocalBroadcastManager.getInstance(baseContext)

            if (heightDiff <= contentViewTop) {
                onHideKeyboard()
                val intent = Intent("KeyboardWillHide")
                broadcastManager.sendBroadcast(intent)
            } else {
                val keyboardHeight = heightDiff - contentViewTop
                onShowKeyboard()
                val intent = Intent("KeyboardWillShow")
                intent.putExtra("KeyboardHeight", keyboardHeight)
                broadcastManager.sendBroadcast(intent)
            }
        }

        keyboardListenersAttached = true
    }

    private fun onHideKeyboard() {
        bottom_guideline.visibility = VISIBLE
        supportFragmentManager.beginTransaction().show(bottom_menu).commit()
        bottom_menu.view!!.visibility = VISIBLE
    }

    private fun onShowKeyboard() {
        bottom_guideline.visibility = GONE
        supportFragmentManager.beginTransaction().hide(bottom_menu).commit()
        bottom_menu.view!!.visibility = GONE
    }

    private fun setupUI(view: View) {
        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                hideSoftKeyboard()
                view.performClick()
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView)
            }
        }
    }

    private fun hideSoftKeyboard() {
        currentFocus?.let {
            val inputMethodManager = ContextCompat.getSystemService(this, InputMethodManager::class.java)!!
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
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
            c_first_stat.visibility = INVISIBLE
            c_last_stat.visibility = INVISIBLE
        }
    }

    override fun setComments(it: List<Comment>) {
        adapter.setComments(it)
    }

    override fun onCommentAdded(comment: Comment) {
        adapter.addComment(comment)
        commentsList.visibility = VISIBLE
        emptyCommentsText.visibility = GONE
        new_comment.text.clear()
    }

    override fun addComment(sendView: View) {
        val current = SystemClock.elapsedRealtime()
        if (current - prevClickTime > 500L) {
            cPresenter.addComment(new_comment.text.toString(), questionId)
            prevClickTime = SystemClock.elapsedRealtime()
        }
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