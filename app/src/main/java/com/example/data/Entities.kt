package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sponsorships")
data class Sponsorship(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sponsorName: String,
    val telegramHandle: String,
    val bidAmountSol: Double,
    val messageText: String,
    val txSignature: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_logs")
data class SystemLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serviceName: String,
    val message: String,
    val logLevel: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "config_entries")
data class ConfigEntry(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "migrated_coins")
data class MigratedCoin(
    @PrimaryKey val tokenAddress: String,
    val symbol: String,
    val name: String,
    val actualMarketCap: Double,
    val athMarketCap: Double,
    val launchedTimestamp: Long,
    val migratedTimestamp: Long,
    val twitterText: String,
    val twitterImageUrl: String,
    val linkedWebsite: String,
    val volumeTraded: Double = 0.0
)

@Entity(tableName = "minted_nfts")
data class MintedNft(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUri: String,
    val quantumConfig: String,
    val walletAddress: String,
    val txSignature: String,
    val timestamp: Long = System.currentTimeMillis()
)
