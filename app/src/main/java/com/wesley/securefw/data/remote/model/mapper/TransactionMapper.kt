package com.wesley.securefw.data.remote.model.mapper

import com.wesley.securefw.data.local.entity.TransactionEntity
import com.wesley.securefw.data.remote.model.TransactionModel
import java.util.Date

fun TransactionEntity.toModel(): TransactionModel {
    return TransactionModel(
        id = id,
        title = title,
        amount = amount,
        category = category,
        date = Date(date),
        type = type,
        userId = userId
    )
}

fun TransactionModel.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        category = category,
        date = date.time,
        type = type,
        userId = userId,
        isSynced = true   // veio da nuvem, então já está sincronizada
    )
}