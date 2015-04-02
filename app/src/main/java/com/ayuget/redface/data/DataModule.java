package com.ayuget.redface.data;

import com.ayuget.redface.ContextModule;
import com.ayuget.redface.data.api.ApiModule;

import dagger.Module;

@Module(
        includes = {
                ApiModule.class,
                ContextModule.class
        }
)
public class DataModule {
}
