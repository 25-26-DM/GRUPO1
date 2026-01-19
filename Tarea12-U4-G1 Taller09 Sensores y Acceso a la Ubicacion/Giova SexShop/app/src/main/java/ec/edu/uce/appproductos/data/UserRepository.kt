package ec.edu.uce.appproductos.data

import ec.edu.uce.appproductos.model.User

class UserRepository(private val userDao: UserDao) {
    suspend fun getUsers(): List<User> = userDao.getAllUsers()

    suspend fun addUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun findUser(username: String, password: String): User? {
        return userDao.findUser(username, password)
    }
}
