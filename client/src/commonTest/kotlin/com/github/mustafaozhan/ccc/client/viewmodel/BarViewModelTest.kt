/*
 * Copyright (c) 2021 Mustafa Ozhan. All rights reserved.
 */
package com.github.mustafaozhan.ccc.client.viewmodel

import com.github.mustafaozhan.ccc.client.base.BaseViewModelTest
import com.github.mustafaozhan.ccc.client.model.Currency
import com.github.mustafaozhan.ccc.client.util.after
import com.github.mustafaozhan.ccc.client.util.before
import com.github.mustafaozhan.ccc.client.viewmodel.bar.BarEffect
import com.github.mustafaozhan.ccc.client.viewmodel.bar.BarViewModel
import com.github.mustafaozhan.ccc.common.di.getDependency
import kotlin.test.Test
import kotlin.test.assertEquals

class BarViewModelTest : BaseViewModelTest<BarViewModel>() {

    override val viewModel: BarViewModel by lazy {
        koin.getDependency(BarViewModel::class)
    }

    @Test
    fun onItemClick() = with(viewModel) {
        val currency = Currency("USD", "Dollar", "$", 0.0, true)
        effect.before {
            event.onItemClick(currency)
        }.after {
            assertEquals(BarEffect.ChangeBase(currency.name), it)
        }
    }

    @Test
    fun onSelectClick() = viewModel.effect.before {
        viewModel.event.onSelectClick()
    }.after {
        assertEquals(BarEffect.OpenCurrencies, it)
    }
}
