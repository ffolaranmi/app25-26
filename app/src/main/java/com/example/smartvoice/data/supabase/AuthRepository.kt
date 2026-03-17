package com.example.smartvoice.data.supabase

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRepository {
    private val supabase = SupabaseClientProvider.client

    suspend fun signUp(email: String, password: String) {
        try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            Log.d("AuthRepository", "Supabase signup succeeded for provider=email")
        } catch (e: Exception) {
            Log.e(
                "AuthRepository",
                "Supabase signup failed: ${e::class.simpleName} - ${e.message}",
                e
            )
            throw e
        }
    }

    suspend fun signIn(email: String, password: String) {
        try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Log.d("AuthRepository", "Supabase signIn succeeded for provider=email")
        } catch (e: Exception) {
            Log.e(
                "AuthRepository",
                "Supabase signIn failed: ${e::class.simpleName} - ${e.message}",
                e
            )
            throw e
        }
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }
}