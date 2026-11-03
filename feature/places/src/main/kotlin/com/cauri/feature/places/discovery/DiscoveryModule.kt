package com.cauri.feature.places.discovery

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DiscoveryModule {
    @Binds
    abstract fun bindPlaceDiscoverer(impl: PlaceDiscovererImpl): PlaceDiscoverer

    @Binds
    abstract fun bindPlaceSuggestionNotifier(impl: PlaceSuggestionNotifierImpl): PlaceSuggestionNotifier
}
