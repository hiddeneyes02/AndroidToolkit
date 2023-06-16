package com.dzboot.template.base

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.multidex.MultiDex
import com.dzboot.template.BuildConfig
import com.dzboot.template.R
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory
import com.zeugmasolutions.localehelper.LocaleHelper
import com.zeugmasolutions.localehelper.LocaleHelperApplicationDelegate
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import timber.log.Timber
import timber.log.Timber.DebugTree


class BaseApp : Application() {

   //region LocaleHelper
   private val localeAppDelegate = LocaleHelperApplicationDelegate()

   override fun attachBaseContext(baseContext: Context) {
      super.attachBaseContext(localeAppDelegate.attachBaseContext(baseContext))
      MultiDex.install(this)
   }

   override fun onConfigurationChanged(newConfig: Configuration) {
      super.onConfigurationChanged(newConfig)
      localeAppDelegate.onConfigurationChanged(this)
   }

   override fun getApplicationContext(): Context = LocaleHelper.onAttach(super.getApplicationContext())
   //endregion


   override fun onCreate() {

      if (MissingSplitsManagerFactory.create(this).disableAppIfMissingRequiredSplits()) {
         // Skip app initialization.
         return
      }

      super.onCreate()
      if (BuildConfig.DEBUG) {
         Timber.plant(DebugTree())
         enableStrictModes()
      }

      ViewPump.init(
				ViewPump.builder().addInterceptor(
						CalligraphyInterceptor(
								CalligraphyConfig.Builder().setDefaultFontPath("fonts/museo_sans_500.otf")
										.setFontAttrId(R.attr.fontPath)
										.build()
						)
				).build()
		)

      //      OneSignal.startInit(this)
      //               .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
      //               .unsubscribeWhenNotificationsAreDisabled(true)
      //               .setNotificationOpenedHandler(new NotificationOpenedHandler(this))
      //               .init();
   }


   private fun enableStrictModes() {
      val policy = VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .penaltyDeath()
            .build()
      StrictMode.setVmPolicy(policy)
   }
}