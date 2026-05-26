package com.example.dacsiii_v2.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.dacsiii_v2.data.model.FavoriteAddress
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.addressBookDataStore by preferencesDataStore(name = "address_book")

object AddressBookStore {
    private val favoritesKey = stringPreferencesKey("favorite_addresses_json")
    private val gson = Gson()
    private val listType = object : TypeToken<List<FavoriteAddress>>() {}.type

    suspend fun getFavorites(context: Context): List<FavoriteAddress> {
        val appContext = context.applicationContext
        return appContext.addressBookDataStore.data
            .map<Preferences, List<FavoriteAddress>> { prefs: Preferences ->
                val json: String? = prefs[favoritesKey]
                if (json.isNullOrBlank()) {
                    emptyList<FavoriteAddress>()
                } else {
                    runCatching { gson.fromJson<List<FavoriteAddress>>(json, listType) }
                        .getOrElse { emptyList<FavoriteAddress>() }
                }
            }
            .first()
    }

    suspend fun saveFavorites(context: Context, favorites: List<FavoriteAddress>) {
        val appContext = context.applicationContext
        val json = gson.toJson(favorites)
        appContext.addressBookDataStore.edit { prefs ->
            prefs[favoritesKey] = json
        }
    }
}
