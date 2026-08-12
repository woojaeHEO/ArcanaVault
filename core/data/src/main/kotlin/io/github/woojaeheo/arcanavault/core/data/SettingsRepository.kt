package io.github.woojaeheo.arcanavault.core.data

import io.github.woojaeheo.arcanavault.core.datastore.UserPreferencesDataSource
import io.github.woojaeheo.arcanavault.core.model.ThemeMode
import io.github.woojaeheo.arcanavault.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
) {
    val preferences: Flow<UserPreferences> = dataSource.preferences

    suspend fun setTheme(mode: ThemeMode) = dataSource.setTheme(mode)
    suspend fun setDynamicColor(enabled: Boolean) = dataSource.setDynamicColor(enabled)
    suspend fun setReducedMotion(enabled: Boolean) = dataSource.setReducedMotion(enabled)
    suspend fun setGridDensity(columns: Int) = dataSource.setGridDensity(columns)
}
