package com.dzboot.template.base

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import com.zeugmasolutions.localehelper.LocaleHelper
import com.zeugmasolutions.localehelper.LocaleHelperActivityDelegateImpl
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.util.*


abstract class BaseActivity : AppCompatActivity() {

   private val localeDelegate = LocaleHelperActivityDelegateImpl()


   @LayoutRes
   abstract fun provideLayout(): Int

   open fun getTopContainer(): View? = null


   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      localeDelegate.onCreate(this)

      //set transparent status bar and navigation bar
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
         window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
         setWindowFlag(true)
      } else {
         setWindowFlag(false)
         window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
         window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
         window.statusBarColor = Color.TRANSPARENT
      }
      window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

      setContentView(provideLayout())

      //applies necessary top margin to prevent views behind status bar
      //        getTopContainer()?.let {
      //            ViewCompat.setOnApplyWindowInsetsListener(it) { v: View, insets: WindowInsetsCompat ->
      //                (v.layoutParams as ViewGroup.MarginLayoutParams).topMargin = insets.systemWindowInsetTop
      //                insets.consumeSystemWindowInsets()
      //            }
      //        }
   }

   override fun getDelegate() = localeDelegate.getAppCompatDelegate(super.getDelegate())

   //TODO use one of these depending on need

   //    ViewPump for font injection
   //    override fun attachBaseContext(newBase: Context?) {
   //        super.attachBaseContext(newBase?.let { ViewPumpContextWrapper.wrap(it) })
   //    }
   //
   //   localeDelegate for locale
   //    override fun attachBaseContext(newBase: Context) {
   //        super.attachBaseContext(localeDelegate.attachBaseContext(newBase))
   //    }
   //
   //   both
   //   override fun attachBaseContext(newBase: Context) {
   //      super.attachBaseContext(ViewPumpContextWrapper.wrap(localeDelegate.attachBaseContext(newBase)))
   //   }

   override fun onResume() {
      super.onResume()
      localeDelegate.onResumed(this)
   }

   override fun onPause() {
      super.onPause()
      localeDelegate.onPaused()
   }

   override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
      return LocaleHelper.onAttach(super.createConfigurationContext(overrideConfiguration))
   }

   override fun getApplicationContext() = localeDelegate.getApplicationContext(super.getApplicationContext())

   open fun updateLocale(locale: Locale) {
      localeDelegate.setLocale(this, locale)
   }

   private fun setWindowFlag(on: Boolean) {
      val winParams: WindowManager.LayoutParams = window.attributes
      winParams.flags =
            if (on) winParams.flags or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            else winParams.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
      window.attributes = winParams
   }

   fun setLightStatusBar() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
         var flags = window.decorView.systemUiVisibility
         flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
         window.decorView.systemUiVisibility = flags
      }
   }

   fun clearLightStatusBar() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
         var flags = window.decorView.systemUiVisibility
         flags = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
         window.decorView.systemUiVisibility = flags
      }
   }

   protected fun getCompatDrawable(@DrawableRes resId: Int) =
         if (resId == 0) null else ResourcesCompat.getDrawable(resources, resId, null)
}