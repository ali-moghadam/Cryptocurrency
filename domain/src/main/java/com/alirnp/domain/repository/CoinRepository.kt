package com.alirnp.domain.repository

import com.alirnp.domain.model.Coin

interface CoinRepository {
    suspend fun getCoins() : List<Coin>
}