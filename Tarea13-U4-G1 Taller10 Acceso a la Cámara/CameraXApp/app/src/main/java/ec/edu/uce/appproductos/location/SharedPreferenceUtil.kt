package ec.edu.uce.appproductos.location

import android.content.Context
import androidx.core.content.edit

internal object SharedPreferenceUtil {
    private const val PREFS_NAME = "ec.edu.uce.appproductos_location_prefs"
    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"
    private const val KEY_USER_NAME = "logged_user_name"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    fun getLocationTrackingPref(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_FOREGROUND_ENABLED, false)

    fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
        }

    // Métodos para persistencia de sesión
    fun saveUserSession(context: Context, userName: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_USER_NAME, userName)
            putBoolean(KEY_IS_LOGGED_IN, true)
        }
    }

    fun getUserSession(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            prefs.getString(KEY_USER_NAME, null)
        } else null
    }

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            remove(KEY_USER_NAME)
            putBoolean(KEY_IS_LOGGED_IN, false)
        }
    }
}
