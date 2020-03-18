package ru.netfantazii.handy.data

class Constants {
    companion object {
        const val GOOGLE_PLAY_SUBSCRIPTION_DEEPLINK_URL =
            "https://play.google.com/store/account/subscriptions?sku=%s&package=%s"
        const val GOOGLE_PLAY_REDEEM_DEEPLINK_URL = "https://play.google.com/redeem?code=%s"
        const val FIRST_LAUNCH_DATE_KEY = "first_launch_date"
        const val LAUNCH_COUNT_KEY = "launch_count"
        const val LAUNCHES_BEFORE_RATE_DIALOG = 7
        const val DAYS_BEFORE_RATE_DIALOG = 2L * 86_400_000L // в миллисекундах
        const val NEVER_SHOW_AGAIN_KEY = "never_show_rate"
    }
}