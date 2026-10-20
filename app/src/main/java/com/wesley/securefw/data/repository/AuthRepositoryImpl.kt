package com.wesley.securefw.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.wesley.securefw.data.local.SecurePreferences
import com.wesley.securefw.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val securePreferences: SecurePreferences
) : AuthRepository {

    private val auth: FirebaseAuth = Firebase.auth

    override suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                securePreferences.saveUserId(user.uid)
                Result.success(user)
            } ?: Result.failure(Exception("Falha ao criar usuário"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                securePreferences.saveUserId(user.uid)
                Result.success(user)
            } ?: Result.failure(Exception("Falha ao fazer login"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            securePreferences.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return securePreferences.isLoggedIn() && getCurrentUser() != null
    }
}