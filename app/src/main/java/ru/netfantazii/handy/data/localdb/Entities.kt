package ru.netfantazii.handy.data.localdb

import android.net.Uri
import androidx.room.*
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.data.model.BuyStatus
import ru.netfantazii.handy.data.model.ExpandStatus
import ru.netfantazii.handy.data.model.GroupType
import java.util.*

abstract class BaseEntity(
    @field:PrimaryKey(autoGenerate = true)
    var id: Long,
    @field:ColumnInfo(name = "creation_time")
    val creationTime: Calendar,
    var position: Int,
    var name: String
)

@Entity
open class CatalogEntity(
    id: Long,
    creationTime: Calendar,
    position: Int,
    name: String,
    @field:ColumnInfo(name = "group_expand_states")
    var groupExpandStates: RecyclerViewExpandableItemManager.SavedState,
    @field:ColumnInfo(name = "alarm_time")
    var alarmTime: Calendar?,
    @field:ColumnInfo(name = "from_network")
    var fromNetwork: Boolean
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
    var catalogId: Long,
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
    var catalogId: Long,
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

@Entity(
    foreignKeys = [ForeignKey(
        entity = CatalogEntity::class,
        parentColumns = ["id"], childColumns = ["catalog_id"], onDelete = ForeignKey.CASCADE
    )]
)
class CatalogNetInfoEntity(
    @field:PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @field:ColumnInfo(name = "catalog_id")
    var catalogId: Long = 0, // изначально 0, потом ИД присваивается сразу после добавления родительского каталога в БД и получения его ИД
    @field:ColumnInfo(name = "receive_time")
    val receiveTime: Calendar,
    @field:ColumnInfo(name = "from_name")
    val fromName: String,
    @field:ColumnInfo(name = "from_email")
    val fromEmail: String,
    @field:ColumnInfo(name = "from_image")
    val fromImage: Uri,
    val commentary: String
)