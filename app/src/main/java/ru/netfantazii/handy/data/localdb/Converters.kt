package ru.netfantazii.handy.data.localdb

import android.net.Uri
import androidx.room.TypeConverter
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.data.model.BuyStatus
import ru.netfantazii.handy.data.model.ExpandStatus
import ru.netfantazii.handy.data.model.GroupType
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

    @TypeConverter
    @JvmStatic
    fun uriToString(uri: Uri) = uri.toString()

    @TypeConverter
    @JvmStatic
    fun stringToUri(string: String) = Uri.parse(string)
}