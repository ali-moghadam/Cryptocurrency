package com.alirnp.cryptocurrency.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alirnp.cryptocurrency.databinding.ItemCoinBinding
import com.alirnp.domain.model.Coin

class CoinAdapter(private var items: List<Coin>) : RecyclerView.Adapter<CoinAdapter.CoinHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinHolder {
       val view = ItemCoinBinding.inflate(LayoutInflater.from(parent.context))
        return CoinHolder(view)
    }

    override fun onBindViewHolder(holder: CoinHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class CoinHolder(private val binding: ItemCoinBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(coin: Coin){
            binding.textViewName.text = coin.name
            binding.textViewSymbol.text = coin.symbol
            binding.textViewRank.text = coin.rank.toString()
            binding.textViewType.text = coin.type
        }
    }
}