package io.github.mdsadiqueinam.hidayah.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.mdsadiqueinam.hidayah.data.AppDatabase
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import io.github.mdsadiqueinam.hidayah.data.ControlledAppDao
import io.github.mdsadiqueinam.hidayah.data.ShieldConfigDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideControlledAppDao(database: AppDatabase): ControlledAppDao {
        return database.controlledAppDao()
    }

    @Provides
    fun provideShieldConfigDao(database: AppDatabase): ShieldConfigDao {
        return database.shieldConfigDao()
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context,
        controlledAppDao: ControlledAppDao,
        shieldConfigDao: ShieldConfigDao
    ): AppRepository {
        return AppRepository(context, controlledAppDao, shieldConfigDao)
    }
}
