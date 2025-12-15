package ec.edu.uce.appproductos.data

import ec.edu.uce.appproductos.model.User
import java.security.MessageDigest

fun hashString(input: String, algorithm: String = "SHA-256"): String {
    return MessageDigest
        .getInstance(algorithm)
        .digest(input.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}

object UserRepository {
    private val users = listOf(
        "Diego" to "Borja",
        "Mateo" to "Jami",
        "John" to "Andino",
        "Kevin" to "Cruz",
        "Anthony" to "Cajamarca",
        "Armando" to "Valle",
        "Wulfer" to "Quiguango"
    ).map { (nombre, apellido) -> User(nombre, apellido, "12345") }.toMutableList()

    fun getUsers(): List<User> = users

    fun addUser(user: User) {
        users.add(user)
    }

    fun findUser(username: String, password: String): User? {
        // The username is now a concatenation of name and lastname, e.g., "DiegoBorja"
        return users.find { (it.nombre + it.apellido).equals(username, ignoreCase = true) && it.password == password }
    }
}
