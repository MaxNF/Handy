package ru.netfantazii.handy.model.database

import androidx.room.TypeConverter
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.model.BuyStatus
import ru.netfantazii.handy.model.ExpandStatus
import ru.netfantazii.handy.model.GroupType
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


//    @TypeConverter
//    @JvmStatic
//    fun calendarToMillis(calendar: Calendar): Long {
//        return calendar.timeInMillis
//    }
//
//    @TypeConverter
//    @JvmStatic
//    fun millisToCalendar(millis: Long): Calendar {
//        val calendar = Calendar.getInstance()
//        calendar.timeInMillis = millis
//        return calendar
//    }


    @TypeConverter
    @JvmStatic
    fun nullableCalendarToMillis(calendar: Calendar?): Long {
        return calendar?.timeInMillis ?: 0L
    }

    @TypeConverter
    @JvmStatic
    fun millisToNullableCalendar(millis: Long): Calendar? {
        if (millis == 0L) return null
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        return calendar
    }

    @TypeConverter
    @JvmStatic
    fun groupExpandStatesToString(groupStates: RecyclerViewExpandableItemManager.SavedState) =
        groupStates.adapterSavedState.joinToString(" ")

    @TypeConverter
    @JvmStatic
    fun stringToGroupExpandStates(stringToParse: String): RecyclerViewExpandableItemManager.SavedState {
        if (stringToParse.isEmpty()) return RecyclerViewExpandableItemManager.SavedState(longArrayOf())
        val stringList = stringToParse.split(" ")
        val array = LongArray(stringList.size) {
                stringList[it].toLong()
        }
        return RecyclerViewExpandableItemManager.SavedState(array)
    }
}