package com.alirnp.domain.useCase

import com.alirnp.domain.core.Resource
import com.alirnp.domain.model.Coin
import com.alirnp.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetCoinUseCase(private val repository: CoinRepository) {

    operator fun invoke()  : Flow<Resource<List<Coin>>> = flow {
       try {
           emit(Resource.Loading())
           val coins = repository.getCoins()
           emit(Resource.Success(data = coins))
       }catch (ex : Exception){
           emit(Resource.Error(message = ex.message ?: "unexpected error occurred"))
       }
    }
}