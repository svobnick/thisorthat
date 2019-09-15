package com.svobnick.thisorthat.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.svobnick.thisorthat.R
import com.svobnick.thisorthat.activities.AnsweredQuestionsActivity
import com.svobnick.thisorthat.activities.FavoriteQuestionsActivity
import com.svobnick.thisorthat.activities.MyQuestionsActivity
import com.svobnick.thisorthat.activities.NewQuestionActivity
import kotlinx.android.synthetic.main.fragment_bottom_dialog.*

class BottomSheetDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_dialog, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        navigation_view.setNavigationItemSelectedListener { menuItem ->
            // Bottom Navigation Drawer menu item clicks
            when (menuItem.itemId) {
                R.id.menu_history_item -> onHistoryButtonClick(context!!)
                R.id.menu_my_questions_item -> onMyQuestionsButtonClick(context!!)
                R.id.favorite_item -> onFavoriteButtonClick(context!!)
                R.id.new_question_item -> onNewQuestionButtonClick(context!!)
            }
            true
        }
    }

    private fun onHistoryButtonClick(context: Context) {
        val intent = Intent(context, AnsweredQuestionsActivity::class.java)
        startActivity(intent)
    }

    private fun onMyQuestionsButtonClick(context: Context) {
        val intent = Intent(context, MyQuestionsActivity::class.java)
        startActivity(intent)
    }

    private fun onFavoriteButtonClick(context: Context) {
        val intent = Intent(context, FavoriteQuestionsActivity::class.java)
        startActivity(intent)
    }

    private fun onNewQuestionButtonClick(context: Context) {
        val intent = Intent(context, NewQuestionActivity::class.java)
        startActivity(intent)
    }
}