package io.github.woojaeheo.holobinder.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.woojaeheo.holobinder.core.model.ThemeMode
import io.github.woojaeheo.holobinder.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferences by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val reducedMotion = booleanPreferencesKey("reduced_motion")
        val gridDensity = intPreferencesKey("grid_density")
    }

    /** 사용자 설정 스트림 */
    val preferences: Flow<UserPreferences> = context.userPreferences.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { values ->
            UserPreferences(
                themeMode = values[Keys.theme]
                    ?.let { stored -> runCatching { ThemeMode.valueOf(stored) }.getOrNull() }
                    ?: ThemeMode.System,
                dynamicColor = values[Keys.dynamicColor] ?: true,
                reducedMotion = values[Keys.reducedMotion] ?: false,
                gridDensity = values[Keys.gridDensity] ?: 2,
            )
        }

    suspend fun setTheme(mode: ThemeMode) {
        context.userPreferences.edit { it[Keys.theme] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.userPreferences.edit { it[Keys.dynamicColor] = enabled }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        context.userPreferences.edit { it[Keys.reducedMotion] = enabled }
    }

    suspend fun setGridDensity(columns: Int) {
        context.userPreferences.edit { it[Keys.gridDensity] = columns.coerceIn(2, 5) }
    }
}
