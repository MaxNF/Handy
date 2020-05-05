package ru.netfantazii.handy.core.catalogs.usecases

import io.reactivex.Completable
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import org.mockito.Mockito
import ru.netfantazii.handy.RxImmediateSchedulerRule
import ru.netfantazii.handy.any
import ru.netfantazii.handy.core.UseCasesTestBase
import ru.netfantazii.handy.core.notifications.alarm.usecases.RegisterAlarmUseCase
import ru.netfantazii.handy.core.notifications.map.usecases.RegisterGeofencesUseCase
import ru.netfantazii.handy.createFakeCatalog
import ru.netfantazii.handy.data.PendingRemovedObject
import java.util.*

class UndoCatalogRemovalUseCaseTest : UseCasesTestBase() {

    companion object {
        @ClassRule
        @JvmField
        val schedulers = RxImmediateSchedulerRule()
    }

    private lateinit var pendingRemovedObject: PendingRemovedObject
    private lateinit var registerAlarmUseCase: RegisterAlarmUseCase
    private lateinit var registerGeofencesUseCase: RegisterGeofencesUseCase
    private lateinit var undoCatalogRemovalUseCase: UndoCatalogRemovalUseCase

    @Before
    fun createUseCase() {
        pendingRemovedObject = PendingRemovedObject()
        pendingRemovedObject.insertEntity(
            createFakeCatalog("1").apply { alarmTime = Calendar.getInstance() }, false)
        registerGeofencesUseCase = Mockito.mock(RegisterGeofencesUseCase::class.java)
        Mockito.`when`(registerGeofencesUseCase.registerGeofences(any(),
            Mockito.anyLong(),
            Mockito.anyString(),
            any()))
            .thenReturn(Completable.complete())
        registerAlarmUseCase = Mockito.mock(RegisterAlarmUseCase::class.java)
        undoCatalogRemovalUseCase = UndoCatalogRemovalUseCase(localRepository,
            pendingRemovedObject,
            registerGeofencesUseCase,
            registerAlarmUseCase)

    }

    @Test
    fun undoRemoval_catalogRemovedFromPendingObject() {
        undoCatalogRemovalUseCase.undoRemoval()?.test()
        assertThat(pendingRemovedObject.entity, `is`(nullValue()))
        Mockito.verify(registerGeofencesUseCase, Mockito.times(1)).registerGeofences(
            any(),
            Mockito.anyLong(),
            Mockito.anyString(),
            any())
        Mockito.verify(registerAlarmUseCase, Mockito.times(1)).registerAlarm(
            Mockito.anyLong(),
            Mockito.anyString(),
            any(),
            any())
    }
}