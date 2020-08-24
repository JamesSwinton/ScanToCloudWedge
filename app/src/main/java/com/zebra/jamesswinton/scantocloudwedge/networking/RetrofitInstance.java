package com.zebra.jamesswinton.scantocloudwedge.networking;

import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {

  private static Retrofit retrofitInstance = null;

  public static Retrofit getInstance(String url) {
    if (retrofitInstance == null) {
      retrofitInstance = new Retrofit.Builder()
          .baseUrl(url)
          .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
          .build();
    } return retrofitInstance;
  }

  public static Retrofit updateInstance(String url) {
      retrofitInstance = new Retrofit.Builder()
              .baseUrl(url)
              .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
              .build();
      return retrofitInstance;
  }

}
