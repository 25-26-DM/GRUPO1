package ec.edu.uce.appproductos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ec.edu.uce.appproductos.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE (nombre || apellido) = :username AND password = :password LIMIT 1")
    suspend fun findUser(username: String, password: String): User?
}
