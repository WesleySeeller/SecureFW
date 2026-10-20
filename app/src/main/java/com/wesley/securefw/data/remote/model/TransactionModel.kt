package com.wesley.securefw.data.remote.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class TransactionModel(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("amount")
    @set:PropertyName("amount")
    var amount: Double = 0.0,

    @get:PropertyName("category")
    @set:PropertyName("category")
    var category: String = "",

    @get:PropertyName("date")
    @set:PropertyName("date")
    var date: Date = Date(),

    @get:PropertyName("type")
    @set:PropertyName("type")
    var type: String = "",

    @get:PropertyName("userId")
    @set:PropertyName("userId")
    var userId: String = ""
)