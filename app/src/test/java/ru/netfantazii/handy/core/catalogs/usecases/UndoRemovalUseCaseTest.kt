package ru.netfantazii.handy.core.catalogs.usecases

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.reactivex.Completable
import io.reactivex.CompletableSource
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import ru.netfantazii.handy.RxImmediateSchedulerRule
import ru.netfantazii.handy.any
import ru.netfantazii.handy.core.notifications.alarm.usecases.RegisterAlarmUseCase
import ru.netfantazii.handy.core.notifications.map.usecases.RegisterGeofencesUseCase
import ru.netfantazii.handy.createFakeCatalog
import ru.netfantazii.handy.data.PendingRemovedObject
import ru.netfantazii.handy.data.database.GeofenceEntity
import java.util.*

class UndoRemovalUseCaseTest : CatalogUseCasesTestBase() {

    companion object {
        @ClassRule
        @JvmField
        val schedulers = RxImmediateSchedulerRule()
    }

    private lateinit var pendingRemovedObject: PendingRemovedObject
    private lateinit var registerAlarmUseCase: RegisterAlarmUseCase
    private lateinit var registerGeofencesUseCase: RegisterGeofencesUseCase
    private lateinit var undoRemovalUseCase: UndoRemovalUseCase

    @Before
    fun createUseCase() {
        pendingRemovedObject = PendingRemovedObject()
        pendingRemovedObject.entity =
            createFakeCatalog("1").apply { alarmTime = Calendar.getInstance() }
        registerGeofencesUseCase = Mockito.mock(RegisterGeofencesUseCase::class.java)
        Mockito.`when`(registerGeofencesUseCase.registerGeofences(any(),
            Mockito.anyLong(),
            Mockito.anyString(),
            any()))
            .thenReturn(Completable.complete())
        registerAlarmUseCase = Mockito.mock(RegisterAlarmUseCase::class.java)
        undoRemovalUseCase = UndoRemovalUseCase(localRepository,
            pendingRemovedObject,
            registerGeofencesUseCase,
            registerAlarmUseCase)

    }

    @Test
    fun undoRemoval_catalogRemovedFromPendingObject() {
        undoRemovalUseCase.undoRemoval()?.test()

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