package com.alirnp.cryptocurrency.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alirnp.cryptocurrency.ui.adapters.CoinAdapter
import com.alirnp.common.viewModel.CoinViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.KoinComponent
import androidx.recyclerview.widget.DividerItemDecoration
import com.alirnp.cryptocurrency.databinding.ActivityMainBinding
import com.alirnp.domain.core.Resource
import com.alirnp.domain.model.Coin


class MainActivity : AppCompatActivity(), KoinComponent  {

    private lateinit var binding : ActivityMainBinding
    private val coinViewModel: CoinViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this)).apply {
            setContentView(this.root)
        }

        observeCoins()
    }

    private fun observeCoins() {
        coinViewModel.coinList.observe(this , { state ->
            when (state){
                is Resource.Success -> {
                    showLoading(false)
                    showData(state.data)
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is Resource.Loading -> showLoading(true)
            }

        })
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showData(data: List<Coin>) {
        val adapter = CoinAdapter(data)
        val layoutManager = LinearLayoutManager(this)

        val dividerItemDecoration = DividerItemDecoration(
            binding.recyclerViewCoin.context,
            layoutManager.orientation
        )

        binding.recyclerViewCoin.setHasFixedSize(true)
        binding.recyclerViewCoin.addItemDecoration(dividerItemDecoration)
        binding.recyclerViewCoin.layoutManager = layoutManager
        binding.recyclerViewCoin.adapter = adapter
    }


    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewCoin.visibility = if (show.not()) View.VISIBLE else View.GONE
    }
}