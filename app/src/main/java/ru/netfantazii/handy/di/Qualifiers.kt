package ru.netfantazii.handy.di

import java.lang.annotation.RetentionPolicy
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationContext

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PackageName

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CatalogId

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CatalogName

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class TotalProducts

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class UnwrappedAdapter

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class WrappedAdapter