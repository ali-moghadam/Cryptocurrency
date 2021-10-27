package com.alirnp.common.di


import com.alirnp.common.viewModel.CoinViewModel
import com.alirnp.data.mapper.CoinMapper
import com.alirnp.data.remote.CoinApi
import com.alirnp.data.repository.CoinRepositoryImpl
import com.alirnp.domain.core.Constants
import com.alirnp.domain.repository.CoinRepository
import com.alirnp.domain.useCase.GetCoinUseCase
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {

    single<CoinApi> {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CoinApi::class.java)
    }

    single { CoinMapper() }

    single<CoinRepository> { CoinRepositoryImpl(get(), get()) }

    single { GetCoinUseCase(get()) }

    viewModel { CoinViewModel(get()) }

}