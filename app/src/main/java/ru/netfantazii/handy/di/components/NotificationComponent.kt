package ru.netfantazii.handy.di.components

import dagger.Subcomponent
import ru.netfantazii.handy.core.notifications.NotificationBroadcastReceiver
import ru.netfantazii.handy.core.notifications.NotificationService
import ru.netfantazii.handy.core.share.CatalogDownloadJobService
import ru.netfantazii.handy.core.share.CatalogMessagingService

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