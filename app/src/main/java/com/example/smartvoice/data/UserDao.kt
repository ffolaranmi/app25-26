package com.example.smartvoice.data

import androidx.room.*

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM user WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM user WHERE username = :username AND password = :password LIMIT 1")
    suspend fun getUserByUsernameAndPassword(username: String, password: String): User?

    @Query("SELECT COUNT(*) FROM user WHERE email = :email")
    suspend fun checkIfEmailExists(email: String): Int

    @Query("SELECT COUNT(*) FROM user WHERE username = :username")
    suspend fun checkIfUsernameExists(username: String): Int

    @Query("SELECT * FROM user ORDER BY id DESC LIMIT 1")
    suspend fun getLatestUser(): User?

    @Query("SELECT * FROM user ORDER BY id DESC")
    suspend fun getAllUsersNewestFirst(): List<User>

    @Query("SELECT * FROM user WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?
}