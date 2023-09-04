package com.yaxan.way.data.di

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import com.yaxan.way.common.Cardinal
import com.yaxan.way.presentation.home_screen.Compass
import com.yaxan.way.presentation.home_screen.SensorDataManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCompass(@ApplicationContext context: Context): Compass {
        return Compass(context)
    }

    @Provides
    @Singleton
    fun provideCardinal(@ApplicationContext context: Context): SensorDataManager {
        return SensorDataManager(context)
    }

    @Provides
    @Singleton
    fun provideSensorManager(application: Application): SensorManager {
        return application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

}