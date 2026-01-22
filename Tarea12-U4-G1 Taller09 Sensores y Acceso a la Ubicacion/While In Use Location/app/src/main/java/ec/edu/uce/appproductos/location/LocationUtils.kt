package ec.edu.uce.appproductos.location

import android.location.Location


fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Ubicación desconocida"
    }
}
