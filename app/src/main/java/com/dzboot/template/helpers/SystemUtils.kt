package com.dzboot.template.helpers

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

object SystemUtils {

	fun Throwable.reportToFirebase(logMessage: String? = null, data : Map<String, Any>? =  null ) =
		with(FirebaseCrashlytics.getInstance()) {
			Timber.e(this@reportToFirebase)
			data?.forEach { setCustomKey(it.key, it.value.toString()) }
			logMessage?.let { log(it) }
			recordException(this@reportToFirebase)
			sendUnsentReports()
		}
}