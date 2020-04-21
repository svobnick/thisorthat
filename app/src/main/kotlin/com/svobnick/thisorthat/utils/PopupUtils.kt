package com.svobnick.thisorthat.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.fragment.app.FragmentActivity
import com.svobnick.thisorthat.R
import kotlinx.android.synthetic.main.popup_choice_added.view.*
import kotlinx.android.synthetic.main.popup_error_view.view.*

object PopupUtils {

    fun setupErrorPopup(context: Context): PopupWindow {
        val popupWindow = PopupWindow(context)
        val reportView = LayoutInflater.from(context).inflate(R.layout.popup_error_view, null)
        popupWindow.contentView = reportView
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.update()
        reportView.error_ok.setOnClickListener {
            popupWindow.dismiss()
        }
        return popupWindow
    }

    fun setupSuccessPopup(context: Context, onDismiss: PopupWindow.OnDismissListener): PopupWindow {
        val popupWindow = PopupWindow(context)
        val reportView = LayoutInflater.from(context).inflate(R.layout.popup_choice_added, null)
        popupWindow.contentView = reportView
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.update()
        reportView.choice_added_ok.setOnClickListener {
            popupWindow.dismiss()
        }
        popupWindow.setOnDismissListener(onDismiss)
        return popupWindow
    }

    fun setupChoicePopup(context: Context, onDismiss: PopupWindow.OnDismissListener): PopupWindow {
        val popupWindow = PopupWindow(context)
        val view = LayoutInflater.from(context).inflate(R.layout.popup_choice_already_exist, null)
        popupWindow.contentView = view
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.update()
        popupWindow.setOnDismissListener(onDismiss)
        return popupWindow
    }

    fun dimBackground(activity: FragmentActivity, container: View) {
        val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.7f
        wm.updateViewLayout(container, p)
    }
}