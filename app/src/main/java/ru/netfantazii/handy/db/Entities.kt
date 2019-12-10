package ru.netfantazii.handy.db

import androidx.room.*
import java.util.*

@Entity
open class CatalogEntity(
    @field:PrimaryKey(autoGenerate = true)
    val id: Long,
    @field:ColumnInfo(name = "creation_time")
    val creationTime: Calendar,
    var position: Int,
    var name: String
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = CatalogEntity::class,
        parentColumns = ["id"], childColumns = ["catalog_id"], onDelete = ForeignKey.CASCADE
    )]
)
open class GroupEntity(
    @field:PrimaryKey(autoGenerate = true)
    val id: Long,
    @field:ColumnInfo(name = "catalog_id")
    val catalogId: Long,
    @field:ColumnInfo(name = "creation_time")
    val creationTime: Calendar,
    @field:ColumnInfo(name = "group_type")
    val groupType: GroupType,
    var position: Int,
    var name: String,
    var expandStatus: ExpandStatus
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = GroupEntity::class,
        parentColumns = ["id"], childColumns = ["group_id"], onDelete = ForeignKey.CASCADE
    )]
)
open class ProductEntity(
    @field:PrimaryKey(autoGenerate = true)
    val id: Long,
    @field:ColumnInfo(name = "catalog_id")
    val catalogId: Long,
    @field:ColumnInfo(name = "group_id")
    val groupId: Long,
    @field:ColumnInfo(name = "creation_time")
    val creationTime: Calendar,
    var position: Int,
    var name: String,
    var buyStatus: BuyStatus
)

