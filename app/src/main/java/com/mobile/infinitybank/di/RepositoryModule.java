package com.mobile.infinitybank.di;

import com.mobile.infinitybank.repository.UserRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {
    @Provides
    @Singleton
    public UserRepository provideUserRepository() {
        return new UserRepository();
    }
}
