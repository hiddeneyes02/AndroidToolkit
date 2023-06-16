package com.dzboot.template

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class AdvancedViewHolder<T : ViewBinding?>(var binding: T) : RecyclerView.ViewHolder(binding!!.root) {

	val context: Context
		get() = itemView.context

	fun getString(@StringRes resId: Int): String {
		return itemView.resources.getString(resId)
	}

	fun getString(@StringRes resId: Int, vararg formatArg: Any?): String {
		return itemView.resources.getString(resId, *formatArg)
	}

	fun getDrawable(@DrawableRes resId: Int): Drawable? {
		return ContextCompat.getDrawable(context, resId)
	}

	@ColorInt
	fun getColor(@ColorRes resId: Int): Int = ContextCompat.getColor(context, resId)
}