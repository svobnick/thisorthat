package com.svobnick.thisorthat.activities

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.squareup.picasso.Picasso
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.adapters.CommentsAdapter
import com.svobnick.thisorthat.adapters.EndlessRecyclerViewScrollListener
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.databinding.ActivityCommentsBinding
import com.svobnick.thisorthat.databinding.PopupErrorViewBinding
import com.svobnick.thisorthat.model.Comment
import com.svobnick.thisorthat.model.SingleQuestionDataViewModel
import com.svobnick.thisorthat.presenters.CommentsPresenter
import com.svobnick.thisorthat.utils.PopupUtils.dimBackground
import com.svobnick.thisorthat.utils.PopupUtils.setupErrorPopup
import com.svobnick.thisorthat.view.CommentsView
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject


class CommentsActivity : MvpAppCompatActivity(), CommentsView {
    private val TAG = this::class.java.name
    private val ANALYTICS_SCREEN_NAME = "Comments"

    @Inject
    lateinit var picasso: Picasso
    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @InjectPresenter
    lateinit var cPresenter: CommentsPresenter

    private lateinit var errorWindow: Pair<PopupWindow, PopupErrorViewBinding>
    private lateinit var adapter: CommentsAdapter

    @ProvidePresenter
    fun providePresenter(): CommentsPresenter {
        return CommentsPresenter(application as ThisOrThatApp)
    }

    private lateinit var binding: ActivityCommentsBinding
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private var keyboardListenersAttached = false

    private var questionId: Long = 0
    private var prevClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as ThisOrThatApp).injector.inject(this)
        super.onCreate(savedInstanceState)

        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cPresenter.attachView(this)
        setupUI(binding.commentsRoot)

        adapter = CommentsAdapter(picasso)
        errorWindow = setupErrorPopup(applicationContext)

        val params = intent.extras!!
        questionId = params.getLong("id")

        binding.commentsList.setHasFixedSize(true)
        binding.commentsList.setItemViewCacheSize(100)
        val linearLayoutManager = LinearLayoutManager(this)
        binding.commentsList.layoutManager = linearLayoutManager
        adapter.setHasStableIds(true)
        binding.commentsList.adapter = adapter
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                cPresenter.getComments(questionId, page * cPresenter.LIMIT)
            }
        }
        binding.commentsList.addOnScrollListener(scrollListener)

        binding.newComment.addTextChangedListener(object : TextWatcher {
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
                    binding.sendComment.isClickable = false
                    binding.sendComment.setImageResource(R.drawable.icon_send_disabled)
                } else {
                    binding.sendComment.isClickable = true
                    binding.sendComment.setImageResource(R.drawable.icon_send)
                }
            }
        })

        attachKeyboardListeners()

        val viewModel = ViewModelProvider(this)[SingleQuestionDataViewModel::class.java]
        fillQuestionFragment(viewModel)
    }

    override fun onStart() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, ANALYTICS_SCREEN_NAME)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS,ANALYTICS_SCREEN_NAME)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        super.onStart()
        cPresenter.getComments(questionId, 0)
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

        binding.commentsRoot.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = binding.commentsRoot.rootView.height - binding.commentsRoot.height
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
        binding.bottomGuideline.visibility = VISIBLE
        supportFragmentManager.beginTransaction().show(supportFragmentManager.findFragmentById(binding.bottomMenu.id)!!).commit()
        binding.bottomMenu.visibility = VISIBLE
    }

    private fun onShowKeyboard() {
        binding.bottomGuideline.visibility = GONE
        supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentById(binding.bottomMenu.id)!!).commit()
        binding.bottomMenu.visibility = GONE
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

    private fun fillQuestionFragment(viewModel: SingleQuestionDataViewModel) {
        val params = intent.extras!!
        viewModel.firstText.value = params.getString("firstText")!!
        viewModel.lastText.value = params.getString("lastText")!!
        viewModel.firstRate.value = params.getString("firstRate")
        viewModel.lastRate.value = params.getString("lastRate")
        viewModel.firstPercent.value = params.getString("firstPercent")
        viewModel.lastPercent.value = params.getString("lastPercent")
        viewModel.choice.value = params.getString("choice")
    }

    override fun setComments(it: List<Comment>) {
        adapter.setComments(it)
    }

    override fun onCommentAdded(comment: Comment) {
        adapter.addComment(comment)
        binding.commentsList.visibility = VISIBLE
        binding.emptyCommentsList.visibility = GONE
        binding.newComment.text.clear()
    }

    override fun addComment(sendView: View) {
        val current = SystemClock.elapsedRealtime()
        if (current - prevClickTime > 500L) {
            cPresenter.addComment(binding.newComment.text.toString(), questionId)
            prevClickTime = SystemClock.elapsedRealtime()
        }
    }

    override fun showEmptyComments() {
        binding.commentsList.visibility = GONE
        binding.emptyCommentsList.visibility = VISIBLE
    }

    override fun showError(errorMsg: String) {
        errorWindow.second.errorText.text = errorMsg
        errorWindow.first.showAtLocation(binding.commentsRoot, Gravity.CENTER, 0, 0)
        dimBackground(this, errorWindow.second.root)
    }
}