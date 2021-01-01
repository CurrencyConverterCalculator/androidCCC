/*
 * Copyright (c) 2020 Mustafa Ozhan. All rights reserved.
 */
package com.github.mustafaozhan.ccc.client.viewmodel.settings

import com.github.mustafaozhan.ccc.client.model.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

// State
data class SettingsState(
    val activeCurrencyCount: Int = 0,
    val appThemeType: AppTheme = AppTheme.SYSTEM_DEFAULT,
    val addFreeDate: String = ""
) {
    companion object {
        fun MutableStateFlow<SettingsState>.update(
            activeCurrencyCount: Int = value.activeCurrencyCount,
            appThemeType: AppTheme = value.appThemeType,
            addFreeDate: String = value.addFreeDate
        ) {
            value = value.copy(
                activeCurrencyCount = activeCurrencyCount,
                appThemeType = appThemeType,
                addFreeDate = addFreeDate
            )
        }
    }
}

// Event
interface SettingsEvent {
    fun onBackClick()
    fun onCurrenciesClick()
    fun onFeedBackClick()
    fun onShareClick()
    fun onSupportUsClick()
    fun onOnGitHubClick()
    fun onRemoveAdsClick()
    fun onSyncClick()
    fun onThemeClick()
}

// Effect
sealed class SettingsEffect
object BackEffect : SettingsEffect()
object CurrenciesEffect : SettingsEffect()
object FeedBackEffect : SettingsEffect()
object ShareEffect : SettingsEffect()
object SupportUsEffect : SettingsEffect()
object OnGitHubEffect : SettingsEffect()
object RemoveAdsEffect : SettingsEffect()
object ThemeDialogEffect : SettingsEffect()
object SynchronisedEffect : SettingsEffect()
object OnlyOneTimeSyncEffect : SettingsEffect()
data class ChangeThemeEffect(val themeValue: Int) : SettingsEffect()

// Data
data class SettingsData(var synced: Boolean = false)