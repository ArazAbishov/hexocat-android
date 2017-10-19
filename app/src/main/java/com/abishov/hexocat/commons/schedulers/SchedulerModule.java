package com.abishov.hexocat.commons.schedulers;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class SchedulerModule {

    @Binds
    @Singleton
    abstract SchedulerProvider schedulerProvider(SchedulerProviderImpl impl);
}
