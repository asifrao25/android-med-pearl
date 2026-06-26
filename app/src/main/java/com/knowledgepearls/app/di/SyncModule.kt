package com.knowledgepearls.app.di

import com.knowledgepearls.app.data.sync.OwnedPublicPearlSyncRunner
import com.knowledgepearls.app.data.sync.OwnedPublicPearlSyncRunnerImpl
import com.knowledgepearls.app.data.sync.PublicFeedStatusSyncRunner
import com.knowledgepearls.app.data.sync.PublicFeedStatusSyncRunnerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    @Binds
    @Singleton
    abstract fun bindOwnedPublicPearlSyncRunner(
        impl: OwnedPublicPearlSyncRunnerImpl,
    ): OwnedPublicPearlSyncRunner

    @Binds
    @Singleton
    abstract fun bindPublicFeedStatusSyncRunner(
        impl: PublicFeedStatusSyncRunnerImpl,
    ): PublicFeedStatusSyncRunner
}
