/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieve of [User] from a given data source.
 */
interface UsersRepository {
    /**
     * Insert user in the data source
     */
    suspend fun insertUser(user: User)

    /**
     * Delete user from the data source
     */
    suspend fun deleteUser(user: User)

    /**
     * Update user in the data source
     */
    suspend fun updateUser(user: User)

    /**
     * Retrieve user from the given data source that matches with the [id].
     */
    fun getUserStream(id: Int): Flow<User>

    /**
     * Retrieve all users from the given data source.
     */
    fun getAllUsersStream(): Flow<List<User>>

    /**
     * Retrieve user by username and password for login
     */
    suspend fun getUserByCredentials(username: String, password: String): User?
}

/**
 * Offline first repository of the User. Implements the methods of the [UsersRepository]
 */
class OfflineUsersRepository(private val userDao: UserDao) : UsersRepository {
    override suspend fun insertUser(user: User) = userDao.insert(user)

    override suspend fun deleteUser(user: User) = userDao.delete(user)

    override suspend fun updateUser(user: User) = userDao.update(user)

    override fun getUserStream(id: Int): Flow<User> = userDao.getUser(id)

    override fun getAllUsersStream(): Flow<List<User>> = userDao.getAllUsers()

    override suspend fun getUserByCredentials(username: String, password: String): User? =
        userDao.getUserByCredentials(username, password)
}
