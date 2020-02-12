package ru.netfantazii.handy.model.database

import androidx.room.*
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.model.BuyStatus
import ru.netfantazii.handy.model.ExpandStatus
import ru.netfantazii.handy.model.GroupType
import java.util.*

abstract class BaseEntity(
    @field:PrimaryKey(autoGenerate = true)
    val id: Long,
    @field:ColumnInfo(name = "creation_time")
    val creationTime: Calendar,
    var position: Int,
    var name: String
)

// todo добавить поле, которое указывает на то, родной каталог или присланный
@Entity
open class CatalogEntity(
    id: Long,
    creationTime: Calendar,
    position: Int,
    name: String,
    @field:ColumnInfo(name = "group_expand_states")
    var groupExpandStates: RecyclerViewExpandableItemManager.SavedState,
    @field:ColumnInfo(name = "alarm_time")
    var alarmTime: Calendar?
) : BaseEntity(id, creationTime, position, name)

@Entity(
    foreignKeys = [ForeignKey(
        entity = CatalogEntity::class,
        parentColumns = ["id"], childColumns = ["catalog_id"], onDelete = ForeignKey.CASCADE
    )]
)
open class GroupEntity(
    id: Long,
    @field:ColumnInfo(name = "catalog_id")
    val catalogId: Long,
    creationTime: Calendar,
    @field:ColumnInfo(name = "group_type")
    val groupType: GroupType,
    position: Int,
    name: String,
    @field:ColumnInfo(name = "expand_status")
    var expandStatus: ExpandStatus
) : BaseEntity(id, creationTime, position, name)

@Entity(
    foreignKeys = [ForeignKey(
        entity = GroupEntity::class,
        parentColumns = ["id"], childColumns = ["group_id"], onDelete = ForeignKey.CASCADE
    )]
)
open class ProductEntity(
    id: Long,
    @field:ColumnInfo(name = "catalog_id")
    val catalogId: Long,
    @field:ColumnInfo(name = "group_id")
    var groupId: Long,
    creationTime: Calendar,
    position: Int,
    name: String,
    @field:ColumnInfo(name = "buy_status")
    var buyStatus: BuyStatus
) : BaseEntity(id, creationTime, position, name)

@Entity(
    foreignKeys = [ForeignKey(
        entity = CatalogEntity::class,
        parentColumns = ["id"], childColumns = ["catalog_id"], onDelete = ForeignKey.CASCADE
    )]
)
class GeofenceEntity(
    @field:PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @field:ColumnInfo(name = "catalog_id")
    val catalogId: Long,
    val latitude: Double,
    val longitude: Double,
    val radius: Float
)