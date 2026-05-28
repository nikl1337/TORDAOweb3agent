package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Sponsorship::class, SystemLog::class, ConfigEntry::class, MigratedCoin::class, MintedNft::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sponsorshipDao(): SponsorshipDao
    abstract fun systemLogDao(): SystemLogDao
    abstract fun configEntryDao(): ConfigEntryDao
    abstract fun migratedCoinDao(): MigratedCoinDao
    abstract fun mintedNftDao(): MintedNftDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tordao_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
