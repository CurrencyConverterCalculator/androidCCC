/*
 * Copyright (c) 2020 Mustafa Ozhan. All rights reserved.
 */
package com.github.mustafaozhan.ccc.common.repo

import com.github.mustafaozhan.ccc.common.base.BaseRepositoryTest
import com.github.mustafaozhan.ccc.common.di.getDependency
import com.github.mustafaozhan.ccc.common.settings.SettingsRepository
import com.github.mustafaozhan.ccc.common.util.nowAsLong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsRepositoryTest : BaseRepositoryTest<SettingsRepository>() {

    override val repository: SettingsRepository by lazy {
        koin.getDependency(SettingsRepository::class)
    }

    // defaults
    @Test
    fun firstRun() = assertEquals(
        true,
        repository.firstRun
    )

    @Test
    fun currentBase() = assertTrue { repository.currentBase.isEmpty() }

    @Test
    fun appTheme() = assertEquals(
        -1,
        repository.appTheme
    )

    @Test
    fun adFreeEndDate() = assertEquals(
        0.toLong(),
        repository.adFreeEndDate
    )

    @Test
    fun lastReviewRequest() = assertTrue {
        repository.lastReviewRequest <= nowAsLong()
    }
}
