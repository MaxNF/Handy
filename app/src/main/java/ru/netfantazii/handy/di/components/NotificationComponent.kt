package ru.netfantazii.handy.di.components

import dagger.Subcomponent
import ru.netfantazii.handy.data.receiver.NotificationBroadcastReceiver
import ru.netfantazii.handy.data.service.NotificationService
import ru.netfantazii.handy.data.service.CatalogDownloadJobService
import ru.netfantazii.handy.data.service.CatalogMessagingService

@Subcomponent
interface NotificationComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): NotificationComponent
    }

    fun inject(notificationService: NotificationService)
    fun inject(notificationBroadcastReceiver: NotificationBroadcastReceiver)
    fun inject(catalogDownloadJobService: CatalogDownloadJobService)
    fun inject(catalogMessagingService: CatalogMessagingService)
}