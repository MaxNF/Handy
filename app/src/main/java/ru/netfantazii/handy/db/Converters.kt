package ru.netfantazii.handy.db

import androidx.room.TypeConverter
import java.util.*

object Converters {

    @TypeConverter
    @JvmStatic
    fun numberToBuyStatus(number: Int): BuyStatus? {
        return BuyStatus.values().firstOrNull { it.number == number }
    }

    @TypeConverter
    @JvmStatic
    fun buyStatusToNumber(buyStatus: BuyStatus): Int {
        return buyStatus.number
    }

    @TypeConverter
    @JvmStatic
    fun numberToExpandStatus(number: Int): ExpandStatus? {
        return ExpandStatus.values().firstOrNull { it.number == number }
    }

    @TypeConverter
    @JvmStatic
    fun expandStatusToNumber(expandStatus: ExpandStatus): Int {
        return expandStatus.number
    }

    @TypeConverter
    @JvmStatic
    fun numberToGroupType(number: Int): GroupType? {
        return GroupType.values().firstOrNull { it.number == number }
    }

    @TypeConverter
    @JvmStatic
    fun groupTypeToNumber(groupType: GroupType): Int {
        return groupType.number
    }



    @TypeConverter
    @JvmStatic
    fun calendarToMillis(calendar: Calendar) : Long {
        return calendar.timeInMillis
    }

    @TypeConverter
    @JvmStatic
    fun millisToCalendar(millis: Long) : Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        return calendar
    }
}