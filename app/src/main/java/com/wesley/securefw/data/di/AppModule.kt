package com.wesley.securefw.data.di

import android.content.Context
import androidx.room.Room
import com.wesley.securefw.data.local.SecurePreferences
import com.wesley.securefw.data.local.dao.TransactionDao
import com.wesley.securefw.data.local.database.AppDatabase
import com.wesley.securefw.data.remote.model.FirestoreService
import com.wesley.securefw.data.repository.AuthRepositoryImpl
import com.wesley.securefw.data.repository.TransactionRepositoryImpl
import com.wesley.securefw.domain.repository.AuthRepository
import com.wesley.securefw.domain.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ---------- Banco de dados ----------
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    // ---------- Firestore ----------
    @Provides
    @Singleton
    fun provideFirestoreService(): FirestoreService {
        return FirestoreService()
    }

    // ---------- Repositório de Transações ----------
    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao,
        firestoreService: FirestoreService
    ): TransactionRepository {
        return TransactionRepositoryImpl(transactionDao, firestoreService)
    }

    // ---------- Preferências Seguras ----------
    @Provides
    @Singleton
    fun provideSecurePreferences(@ApplicationContext context: Context): SecurePreferences {
        return SecurePreferences(context)
    }

    // ---------- Repositório de Autenticação ----------
    @Provides
    @Singleton
    fun provideAuthRepository(securePreferences: SecurePreferences): AuthRepository {
        return AuthRepositoryImpl(securePreferences)
    }
}