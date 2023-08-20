package com.svobnick.thisorthat.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.fragment.app.FragmentActivity
import com.svobnick.thisorthat.databinding.PopupChoiceAddedBinding
import com.svobnick.thisorthat.databinding.PopupChoiceAlreadyExistBinding
import com.svobnick.thisorthat.databinding.PopupErrorViewBinding

object PopupUtils {

    fun setupErrorPopup(context: Context): Pair<PopupWindow, PopupErrorViewBinding> {
        val popupWindow = PopupWindow(context)
        val binding = PopupErrorViewBinding.inflate(LayoutInflater.from(context))
        popupWindow.contentView = binding.root
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.update()
        binding.errorOk.setOnClickListener {
            popupWindow.dismiss()
        }
        return Pair(popupWindow, binding)
    }

    fun setupSuccessPopup(context: Context, onDismiss: PopupWindow.OnDismissListener): Pair<PopupWindow, PopupChoiceAddedBinding> {
        val popupWindow = PopupWindow(context)
        val binding = PopupChoiceAddedBinding.inflate(LayoutInflater.from(context))
        popupWindow.contentView = binding.root
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.update()
        binding.choiceAddedOk.setOnClickListener {
            popupWindow.dismiss()
        }
        popupWindow.setOnDismissListener(onDismiss)
        return Pair(popupWindow, binding)
    }

    fun setupChoicePopup(context: Context, onDismiss: PopupWindow.OnDismissListener): Pair<PopupWindow, PopupChoiceAlreadyExistBinding> {
        val popupWindow = PopupWindow(context)
        val binding = PopupChoiceAlreadyExistBinding.inflate(LayoutInflater.from(context))
        popupWindow.contentView = binding.root
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.update()
        popupWindow.setOnDismissListener(onDismiss)
        return Pair(popupWindow,binding)
    }

    fun dimBackground(activity: FragmentActivity, container: View) {
        val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.7f
        wm.updateViewLayout(container, p)
    }
}