package com.svobnick.thisorthat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.svobnick.thisorthat.activities.HistoryChoiceActivity
import com.svobnick.thisorthat.adapters.EndlessRecyclerViewScrollListener
import com.svobnick.thisorthat.adapters.MyQuestionsAdapter
import com.svobnick.thisorthat.app.ThisOrThatApp
import com.svobnick.thisorthat.databinding.FragmentQuestionsListBinding
import com.svobnick.thisorthat.model.Question
import com.svobnick.thisorthat.presenters.ProfilePresenter
import com.svobnick.thisorthat.view.OnItemClickListener
import moxy.MvpAppCompatFragment
import javax.inject.Inject

class MyQuestionsListFragment : MvpAppCompatFragment(),
    OnItemClickListener {

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    lateinit var presenter: ProfilePresenter

    lateinit var adapter: MyQuestionsAdapter
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener

    private var _binding: FragmentQuestionsListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity().application as ThisOrThatApp).injector.inject(this)
        _binding = FragmentQuestionsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = MyQuestionsAdapter(this)

        val linearLayoutManager = LinearLayoutManager(context)
        binding.questionsList.layoutManager = linearLayoutManager
        adapter.setHasStableIds(true)
        binding.questionsList.adapter = adapter

        presenter = (parentFragment as ProfileFragment).profilePresenter

        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                presenter.getMyQuestions(page * presenter.MY_QUESTIONS_LIMIT)
            }
        }
        binding.questionsList.addOnScrollListener(scrollListener)
    }

    override fun onResume() {
        super.onResume()
        firebaseAnalytics.logEvent("open_user_questions", null)
        adapter.clear()
        scrollListener.resetState()
        presenter.getMyQuestions(0)
    }

    fun addQuestionsToList(questions: List<Question>) {
        adapter.addQuestions(questions)
        binding.questionsList.visibility = View.VISIBLE
        binding.emptyQuestionList.visibility = View.GONE
    }

    override fun onItemClick(position: Int, favorite: Boolean) {
        val item = adapter.getItem(position)
        val intent = Intent(context, HistoryChoiceActivity::class.java)
        intent.putExtra("itemId", item.id)
        intent.putExtra("firstText", item.firstText)
        intent.putExtra("lastText", item.lastText)
        intent.putExtra("firstRate", item.firstRate)
        intent.putExtra("lastRate", item.lastRate)
        intent.putExtra("status", item.status)
        intent.putExtra("choice", Question.Choices.MY_QUESTION)
        intent.putExtra("isFavorite", favorite)
        startActivity(intent)
    }

    fun showEmptyList() {
        binding.questionsList.visibility = View.GONE
        binding.emptyQuestionList.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}