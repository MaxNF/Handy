package ru.netfantazii.handy.data

class Constants {
    companion object {
        const val GOOGLE_PLAY_SUBSCRIPTION_DEEPLINK_URL =
            "https://play.google.com/store/account/subscriptions?sku=%s&package=%s"
        const val GOOGLE_PLAY_REDEEM_DEEPLINK_URL = "https://play.google.com/redeem?code=%s"
    }
}