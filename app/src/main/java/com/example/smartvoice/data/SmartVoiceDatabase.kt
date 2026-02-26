package com.example.smartvoice.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        DiagnosisTable::class
    ],
    version = 5,
    exportSchema = false
)
abstract class SmartVoiceDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun diagnosisDao(): DiagnosisDao

    companion object {
        @Volatile
        private var instance: SmartVoiceDatabase? = null

        fun getInstance(context: Context): SmartVoiceDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): SmartVoiceDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                SmartVoiceDatabase::class.java,
                "smart_voice_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}