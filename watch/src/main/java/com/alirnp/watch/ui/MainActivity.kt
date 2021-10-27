package com.alirnp.watch.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.wear.widget.WearableLinearLayoutManager
import com.alirnp.domain.core.Resource
import com.alirnp.domain.model.Coin
import com.alirnp.watch.databinding.ActivityMainBinding
import com.alirnp.watch.ui.adapters.CoinAdapter
import com.alirnp.watch.viewModel.CoinViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.KoinComponent

class MainActivity : AppCompatActivity() , KoinComponent{

    private lateinit var binding: ActivityMainBinding
    private val coinViewModel: CoinViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
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
        val layoutManager = WearableLinearLayoutManager(this)

        val dividerItemDecoration = DividerItemDecoration(
            binding.wearableRecyclerViewCoins.context,
            layoutManager.orientation
        )

        binding.wearableRecyclerViewCoins.setHasFixedSize(true)
        binding.wearableRecyclerViewCoins.isEdgeItemsCenteringEnabled = true
        binding.wearableRecyclerViewCoins.addItemDecoration(dividerItemDecoration)
        binding.wearableRecyclerViewCoins.layoutManager = layoutManager
        binding.wearableRecyclerViewCoins.adapter = adapter
    }


    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.wearableRecyclerViewCoins.visibility = if (show.not()) View.VISIBLE else View.GONE
    }
}