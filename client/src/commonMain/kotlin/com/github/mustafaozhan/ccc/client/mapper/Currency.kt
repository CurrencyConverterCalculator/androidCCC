package com.github.mustafaozhan.ccc.client.mapper

import com.github.mustafaozhan.ccc.common.model.Currency
import com.github.mustafaozhan.ccc.client.model.Currency as CurrencyUIModel

fun Currency.toUIModel() = CurrencyUIModel(
    name = name,
    longName = longName,
    symbol = symbol,
    rate = rate,
    isActive = isActive
)

fun List<Currency>.toUIModelList() = map {
    it.toUIModel()
}
