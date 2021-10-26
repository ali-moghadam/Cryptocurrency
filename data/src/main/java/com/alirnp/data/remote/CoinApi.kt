package com.alirnp.data.remote

import com.alirnp.data.remote.model.CoinEntity
import retrofit2.http.GET

interface CoinApi {
    @GET("/v1/coins")
    suspend fun getCoins(): List<CoinEntity>
}