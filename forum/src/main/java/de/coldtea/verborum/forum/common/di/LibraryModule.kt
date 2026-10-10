package de.coldtea.verborum.forum.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.coldtea.verborum.core.library.domain.MarketplaceRatings
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceRatingService

/** Exposes the Forum's marketplace features to other modules through core's contracts. */
@InstallIn(SingletonComponent::class)
@Module
abstract class LibraryModule {

    @Binds
    abstract fun bindMarketplaceRatings(service: MarketplaceRatingService): MarketplaceRatings
}
