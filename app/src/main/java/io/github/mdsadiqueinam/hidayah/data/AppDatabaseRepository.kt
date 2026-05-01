package io.github.mdsadiqueinam.hidayah.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDatabaseRepository @Inject constructor(
    private val controlledAppDao: ControlledAppDao,
    private val shieldConfigDao: ShieldConfigDao
) {
    fun getControlledApps(): Flow<List<ControlledApp>> = controlledAppDao.getAllControlledApps()

    suspend fun getControlledApp(packageName: String): ControlledApp? =
        controlledAppDao.getControlledApp(packageName)

    fun getControlledAppFlow(packageName: String): Flow<ControlledApp?> =
        controlledAppDao.getControlledAppFlow(packageName)

    suspend fun saveControlledApps(apps: List<ControlledApp>) {
        controlledAppDao.deleteAll()
        controlledAppDao.insertAll(apps)
    }

    suspend fun updateControlledApp(app: ControlledApp) = controlledAppDao.update(app)

    suspend fun removeControlledApp(packageName: String) = controlledAppDao.delete(packageName)

    suspend fun getControlledPackageNames(): List<String> = controlledAppDao.getAllPackageNames()

    fun getShieldConfig(): Flow<ShieldConfig?> = shieldConfigDao.getShieldConfig()

    suspend fun updateShieldConfig(config: ShieldConfig) = shieldConfigDao.insertOrUpdate(config)
}
