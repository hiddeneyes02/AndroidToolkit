package com.dzboot.template.remote

import android.content.Context
import androidx.annotation.NonNull
import com.dzboot.template.base.BaseFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


abstract class NetworkCallback<T>(private val context: Context) : Callback<T> {

   private var fragment: BaseFragment? = null

   constructor(@NonNull fragment: BaseFragment) : this(fragment.requireContext()) {
      this.fragment = fragment
   }

   final override fun onResponse(call: Call<T>, response: Response<T>) {
      if (fragment != null && !fragment!!.isAdded)
         return

      val code = response.code()
      if (code in 200..210) {
         onResult(response.body())
         return
      }

      onFailure(Throwable("Server response error: $code"), code)
   }

   final override fun onFailure(call: Call<T>, t: Throwable) {
      if (fragment != null && !fragment!!.isAdded)
         return

      onFailure(t, 0 /*not a server error*/)
   }

   abstract fun onResult(result: T?)

   open fun onFailure(t: Throwable, responseCode: Int) {
      Timber.e(t)
   }
}