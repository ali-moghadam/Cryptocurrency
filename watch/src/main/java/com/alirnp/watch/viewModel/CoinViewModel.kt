package com.alirnp.watch.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alirnp.domain.core.Resource
import com.alirnp.domain.model.Coin
import com.alirnp.domain.useCase.GetCoinUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CoinViewModel(private val getCoinUseCase: GetCoinUseCase) : ViewModel() {

    private var _coinList = MutableLiveData<Resource<List<Coin>>>()
    var coinList : LiveData<Resource<List<Coin>>> = _coinList

    init {
        getCoins()
    }

    private fun getCoins() {
        getCoinUseCase().onEach { result ->
           _coinList.value = result
        }.launchIn(viewModelScope)
    }
}