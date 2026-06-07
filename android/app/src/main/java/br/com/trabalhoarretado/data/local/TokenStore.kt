package br.com.trabalhoarretado.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenStore(
    private val context: Context,
) {
    private val tokenKey = stringPreferencesKey("auth_token")
    private val userIdKey = stringPreferencesKey("user_id")
    private val userRoleKey = stringPreferencesKey("user_role")

    val tokenFlow: Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[tokenKey] }

    suspend fun getToken(): String? = tokenFlow.first()

    suspend fun getUserId(): String? = context.dataStore.data.map { it[userIdKey] }.first()

    suspend fun getUserRole(): String? = context.dataStore.data.map { it[userRoleKey] }.first()

    suspend fun setSession(
        token: String,
        userId: String,
        role: String,
    ) {
        context.dataStore.edit {
            it[tokenKey] = token
            it[userIdKey] = userId
            it[userRoleKey] = role
        }
    }

    suspend fun setToken(token: String) {
        context.dataStore.edit { it[tokenKey] = token }
    }

    suspend fun clear() {
        context.dataStore.edit {
            it.remove(tokenKey)
            it.remove(userIdKey)
            it.remove(userRoleKey)
        }
    }
}
