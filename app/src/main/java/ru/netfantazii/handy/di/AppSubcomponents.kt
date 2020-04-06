package ru.netfantazii.handy.di

import dagger.Module
import ru.netfantazii.handy.di.components.CatalogsComponent
import ru.netfantazii.handy.di.components.GroupsAndProductsComponent

@Module(subcomponents = [GroupsAndProductsComponent::class, CatalogsComponent::class])
class AppSubcomponents