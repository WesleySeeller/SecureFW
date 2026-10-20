package com.wesley.securefw.domain.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun signUp(email: String, password: String): Result<FirebaseUser>
    suspend fun signIn(email: String, password: String): Result<FirebaseUser>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUser(): FirebaseUser?
    suspend fun isUserLoggedIn(): Boolean
}

