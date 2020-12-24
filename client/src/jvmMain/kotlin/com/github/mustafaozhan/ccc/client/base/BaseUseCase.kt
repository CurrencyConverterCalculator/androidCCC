/*
 * Copyright (c) 2020 Mustafa Ozhan. All rights reserved.
 */

package com.github.mustafaozhan.ccc.client.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren

@Suppress("EmptyDefaultConstructor")
actual open class BaseUseCase actual constructor() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope: CoroutineScope = GlobalScope

    protected actual val scope: CoroutineScope = viewModelScope

    actual open fun onDestroy() {
        viewModelJob.cancelChildren()
    }
}
