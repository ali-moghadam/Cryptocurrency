package com.alirnp.data.repository

import com.alirnp.data.mapper.CoinMapper
import com.alirnp.data.remote.CoinApi
import com.alirnp.domain.repository.CoinRepository

class CoinRepositoryImpl constructor(private val coinApi: CoinApi , private val coinMapper: CoinMapper) : CoinRepository {

    override suspend fun getCoins() = coinApi.getCoins().map { coinMapper.fromEntity(it) }
}