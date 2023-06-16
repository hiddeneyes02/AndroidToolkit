package com.dzboot.template.helpers

import android.R
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import java.util.*


object DisplayUtils {

	fun getDisplayMetrics(activity: AppCompatActivity): DisplayMetrics {
		val displayMetrics = DisplayMetrics()
		activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
		return displayMetrics
	}

	/**
	 * Get the height of status bar
	 *
	 * @param context Context
	 * @return height of status bar
	 */
	fun getStatusBarHeight(context: Context): Int {
		var statusBarHeight = 0
		val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
		if (resourceId > 0) {
			statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
		}
		return statusBarHeight
	}

	/**
	 * Get the height of action bar
	 *
	 * @param context Context
	 * @return height of action bar
	 */
	fun getActionBarHeight(context: Context): Int {
		var actionBarHeight = 0
		val styledAttributes = context.theme.obtainStyledAttributes(intArrayOf(R.attr.actionBarSize))
		actionBarHeight = styledAttributes.getDimension(0, 0f).toInt()
		styledAttributes.recycle()
		return actionBarHeight
	}

	/**
	 * Get the height of notification bar
	 *
	 * @param context Context
	 * @return height of notification bar
	 */
	fun getNotificationBarHeight(context: Context): Int {
		var navigationBarHeight = 0
		val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
		if (resourceId > 0) {
			navigationBarHeight = context.resources.getDimensionPixelSize(resourceId)
		}
		return navigationBarHeight
	}

	/**
	 * make status bar transparent without messing the soft buttons (back, home, ...) on the bottom of some devices
	 *
	 * @param activity the activity
	 */
	fun setTransparentStatusBar(activity: AppCompatActivity) {
		//make full transparent statusBar
		if (Build.VERSION.SDK_INT in 19..20) {
			setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
		}
		if (Build.VERSION.SDK_INT >= 19) {
			activity.window
					.decorView.systemUiVisibility =
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		}
		if (Build.VERSION.SDK_INT >= 21) {
			setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
			activity.window.statusBarColor = Color.TRANSPARENT
		}
	}

	/**
	 * Changes the status bar color
	 *
	 * @param activity the activity
	 * @param color    the color as resource id
	 */
	fun changeStatusBarColor(activity: AppCompatActivity, @ColorRes color: Int) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			val window = activity.window
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			window.statusBarColor = activity.resources.getColor(color)
		}
	}

	/**
	 * Sets window flag
	 *
	 * @param activity the activity
	 * @param bits     bit to set
	 * @param on       of or off
	 */
	private fun setWindowFlag(activity: AppCompatActivity, bits: Int, on: Boolean) {
		val win = activity.window
		val winParams = win.attributes
		if (on) {
			winParams.flags = winParams.flags or bits
		} else {
			winParams.flags = winParams.flags and bits.inv()
		}
		win.attributes = winParams
	}

	/**
	 * Hide soft keyboard using a view
	 *
	 * @param view The view
	 */
	fun hideSoftKeyboard(view: View?) {
		if (view != null) {
			val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			imm?.hideSoftInputFromWindow(view.windowToken, 0)
		}
	}

	/**
	 * Hide soft keyboard using the activity
	 *
	 * @param activity the activity
	 */
	fun hideSoftKeyboard(activity: Activity) {
		val imm = activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
		//Find the currently focused view, so we can grab the correct window token from it.
		var view = activity.currentFocus
		//If no view currently has focus, create a new one, just so we can grab a window token from it
		if (view == null) {
			view = View(activity)
		}
		imm?.hideSoftInputFromWindow(view.windowToken, 0)
	}

	/**
	 * Converts dp to px
	 *
	 * @param context the context
	 * @param dp      dp
	 * @return px
	 */
   @JvmStatic
   fun dpToPx(context: Context, dp: Float): Int {
		return TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, dp,
				context.resources.displayMetrics
		).toInt()
	}

	/**
	 * Converts sp to px
	 *
	 * @param context the context
	 * @param sp      sp
	 * @return px
	 */
	fun spToPx(context: Context, sp: Float): Int {
		return TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP,
				sp,
				context.resources.displayMetrics
		).toInt()
	}

	/**
	 * Converts dp to sp
	 *
	 * @param context the context
	 * @param dp      dp
	 * @return sp
	 */
	fun dpToSp(context: Context, dp: Float): Int {
		return (dpToPx(context, dp) / context.resources.displayMetrics.scaledDensity).toInt()
	}

	/**
	 * Checks if the locale is RTL
	 *
	 * @param locale the locale
	 * @return true if RTL, false if not
	 */
	fun isRTL(locale: Locale): Boolean {
		val directionality = Character.getDirectionality(locale.displayName[0]).toInt()
		return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT.toInt() || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC.toInt()
	}

	/**
	 * Checks if currently display is RTL
	 *
	 * @return true if RTL, false if not
	 */
	val isRTL: Boolean
		get() {
			val directionality = Character.getDirectionality(Locale.getDefault().displayName[0]).toInt()
			return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT.toInt() || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC.toInt()
		}

	/**
	 * Shows a popup window inflated from the provided layout resource id
	 *
	 * @param activity         the activity
	 * @param layout           the popup window layout
	 * @param centerVertical   whether to center the window vertically on screen
	 * @param centerHorizontal whether to center the window horizontally on screen
	 * @param view             any view to get the token from
	 * @return the PopupWindow object
	 */
	fun showPopupWindow(
			activity: AppCompatActivity, @LayoutRes layout: Int,
			centerVertical: Boolean, centerHorizontal: Boolean, view: View?
	): PopupWindow {
		val display = activity.windowManager.defaultDisplay
		val size = Point()
		display.getSize(size)
		val popupView = LayoutInflater.from(activity).inflate(layout, null)
		popupView.measure(
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
		)
		val popupWindow = PopupWindow(popupView)
		popupWindow.width = if (centerHorizontal) popupView.measuredWidth else size.x - 100
		popupWindow.height = if (centerVertical) popupView.measuredHeight else size.y - 100
		popupWindow.isFocusable = true
		popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
		val container = popupWindow.contentView.rootView
		val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
		val p = container.layoutParams as WindowManager.LayoutParams
		p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
		p.dimAmount = 0.3f
		wm.updateViewLayout(container, p)
		return popupWindow
	}

	fun Activity.getScreenWidth(): Int {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			val windowMetrics = windowManager.currentWindowMetrics
			val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
			windowMetrics.bounds.width() - insets.left - insets.right
		} else {
			val displayMetrics = DisplayMetrics()
			windowManager.defaultDisplay.getMetrics(displayMetrics)
			displayMetrics.widthPixels
		}
	}

	fun Activity.getScreenHeight(): Int {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			val windowMetrics = windowManager.currentWindowMetrics
			val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
			windowMetrics.bounds.height() - insets.top - insets.bottom
		} else {
			val displayMetrics = DisplayMetrics()
			windowManager.defaultDisplay.getMetrics(displayMetrics)
			displayMetrics.heightPixels
		}
	}
}