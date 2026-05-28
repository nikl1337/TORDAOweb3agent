package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SponsorshipDao {
    @Query("SELECT * FROM sponsorships ORDER BY timestamp DESC")
    fun getAllSponsorships(): Flow<List<Sponsorship>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSponsorship(sponsorship: Sponsorship)

    @Query("DELETE FROM sponsorships")
    suspend fun clearAll()
}

@Dao
interface SystemLogDao {
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<SystemLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SystemLog)

    @Query("DELETE FROM system_logs")
    suspend fun clearAll()
}

@Dao
interface ConfigEntryDao {
    @Query("SELECT value FROM config_entries WHERE `key` = :key")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(entry: ConfigEntry)
}

@Dao
interface MigratedCoinDao {
    @Query("SELECT * FROM migrated_coins ORDER BY migratedTimestamp DESC")
    fun getAllMigratedCoins(): Flow<List<MigratedCoin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMigratedCoin(coin: MigratedCoin)

    @Query("DELETE FROM migrated_coins")
    suspend fun clearAll()
}

@Dao
interface MintedNftDao {
    @Query("SELECT * FROM minted_nfts ORDER BY timestamp DESC")
    fun getAllMintedNfts(): Flow<List<MintedNft>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMintedNft(nft: MintedNft)

    @Query("DELETE FROM minted_nfts")
    suspend fun clearAll()
}
