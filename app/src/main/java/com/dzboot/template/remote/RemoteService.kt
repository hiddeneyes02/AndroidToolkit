package com.dzboot.template.remote

import com.dzboot.template.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


//TODO needs more works
class RemoteService<D>(private val baseUrl: String) {

	companion object {

		private const val DEFAULT_BASE_URL = ""

		val defaultApiService: DefaultApiService
			get() {
				return RemoteService<DefaultApiService>(DEFAULT_BASE_URL).getApi(DefaultApiService::class.java)
			}
	}

	private fun getApi(service: Class<D>): D {
		val logging = HttpLoggingInterceptor()
		logging.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
		val client = OkHttpClient.Builder().addInterceptor(logging).build()

		return Retrofit.Builder()
				.addConverterFactory(GsonConverterFactory.create())
				.baseUrl(baseUrl)
				.client(client)
				.build()
				.create(service)
	}
}
