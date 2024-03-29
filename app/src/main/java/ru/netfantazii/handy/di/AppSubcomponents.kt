package ru.netfantazii.handy.di

import dagger.Module
import ru.netfantazii.handy.di.components.*

@Module(subcomponents = [GroupsAndProductsComponent::class,
    CatalogsComponent::class,
    MainComponent::class,
    ShareComponent::class,
    NotificationComponent::class,
    AlarmComponent::class,
    MapComponent::class])
class AppSubcomponents