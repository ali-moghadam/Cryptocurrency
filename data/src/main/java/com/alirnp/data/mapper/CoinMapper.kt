package com.alirnp.data.mapper

import com.alirnp.data.remote.model.CoinEntity
import com.alirnp.domain.model.Coin

class CoinMapper : Mapper<CoinEntity, Coin> {
    override fun fromEntity(entity: CoinEntity): Coin {
        return Coin(
            id = entity.id,
            isActive = entity.isActive,
            isNew = entity.isNew,
            name = entity.name,
            rank = entity.rank,
            symbol = entity.symbol,
            type = entity.type
        )
    }

    override fun toEntity(model: Coin): CoinEntity {
        return CoinEntity(
            id = model.id,
            isActive = model.isActive,
            isNew = model.isNew,
            name = model.name,
            rank = model.rank,
            symbol = model.symbol,
            type = model.type
        )
    }
}