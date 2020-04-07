package ru.netfantazii.handy.di

import java.lang.annotation.RetentionPolicy
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationContext

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PackageName

//@Qualifier
//@Retention(AnnotationRetention.RUNTIME)
//annotation class CatalogsStorage