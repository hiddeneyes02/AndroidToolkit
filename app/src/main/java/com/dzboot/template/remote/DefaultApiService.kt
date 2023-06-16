package com.dzboot.template.remote

import retrofit2.Call
import retrofit2.http.GET


interface DefaultApiService {

   @GET("list.php")
   fun getLocations(): Call<List<String>>
}