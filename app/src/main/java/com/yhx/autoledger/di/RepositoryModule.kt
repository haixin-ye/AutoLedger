package com.yhx.autoledger.di

import com.yhx.autoledger.data.repository.AIPersonaRepository
import com.yhx.autoledger.data.repository.LocalAIPersonaRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// 注意：这里用的是 abstract class 和 @Binds，这是 Hilt 绑定接口和实现类的标准写法
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAIPersonaRepository(
        localImpl: LocalAIPersonaRepositoryImpl
    ): AIPersonaRepository

}