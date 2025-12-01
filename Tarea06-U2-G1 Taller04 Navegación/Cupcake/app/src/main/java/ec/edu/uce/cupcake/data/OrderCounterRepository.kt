package ec.edu.uce.cupcake.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "order_counter")

class OrderCounterRepository(private val context: Context) {

    private val ORDER_COUNTER_KEY = intPreferencesKey("order_counter")

    val orderCounter: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[ORDER_COUNTER_KEY] ?: 0
        }

    suspend fun incrementOrderCounter() {
        context.dataStore.edit { preferences ->
            val currentCounter = preferences[ORDER_COUNTER_KEY] ?: 0
            preferences[ORDER_COUNTER_KEY] = currentCounter + 1
        }
    }
}

