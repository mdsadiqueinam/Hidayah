package io.github.mdsadiqueinam.hidayah.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ControlledAppDao {
    @Query("SELECT * FROM controlled_apps ORDER BY appName ASC")
    fun getAllControlledApps(): Flow<List<ControlledApp>>

    @Query("SELECT * FROM controlled_apps WHERE packageName = :packageName")
    suspend fun getControlledApp(packageName: String): ControlledApp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<ControlledApp>)

    @Update
    suspend fun update(app: ControlledApp)

    @Query("DELETE FROM controlled_apps WHERE packageName = :packageName")
    suspend fun delete(packageName: String)

    @Query("DELETE FROM controlled_apps")
    suspend fun deleteAll()

    @Query("SELECT packageName FROM controlled_apps")
    suspend fun getAllPackageNames(): List<String>
}
