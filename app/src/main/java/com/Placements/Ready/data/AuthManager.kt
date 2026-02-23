package com.Placements.Ready.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = currentUser != null
    val isEmailVerified: Boolean get() = currentUser?.isEmailVerified == true

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            result.user?.let { Result.success(it) }
                ?: Result.failure(Exception("Sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            result.user?.let {
                // Send verification email immediately after sign-up
                it.sendEmailVerification().await()
                Result.success(it)
            } ?: Result.failure(Exception("Sign up failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user?.let { Result.success(it) }
                ?: Result.failure(Exception("Google sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut(context: android.content.Context) {
        auth.signOut()
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        ).build()
        com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso).signOut()
    }

    suspend fun sendVerificationEmail(): Result<Unit> {
        return try {
            val user = currentUser
                ?: return Result.failure(Exception("No user logged in"))
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reloadUser(): Result<Boolean> {
        return try {
            currentUser?.reload()?.await()
                ?: return Result.failure(Exception("No user logged in"))
            Result.success(auth.currentUser?.isEmailVerified == true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
