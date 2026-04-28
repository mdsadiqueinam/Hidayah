package io.github.mdsadiqueinam.hidayah.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShieldConfigDao {
    @Query("SELECT * FROM shield_config WHERE id = 1")
    fun getShieldConfig(): Flow<ShieldConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: ShieldConfig)
}
