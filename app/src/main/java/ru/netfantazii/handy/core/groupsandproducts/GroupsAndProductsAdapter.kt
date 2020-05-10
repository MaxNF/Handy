package ru.netfantazii.handy.core.groupsandproducts

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.expandable.*
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import ru.netfantazii.handy.R
import ru.netfantazii.handy.databinding.RvGroupElementBinding
import ru.netfantazii.handy.databinding.RvProductElementBinding
import ru.netfantazii.handy.databinding.RvUnsortedGroupElementBinding
import ru.netfantazii.handy.data.BuyStatus
import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.data.GroupType
import ru.netfantazii.handy.data.Product
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.di.UnwrappedAdapter
import ru.netfantazii.handy.di.WrappedAdapter
import java.lang.UnsupportedOperationException
import javax.inject.Inject

interface ProductClickHandler {
    fun onProductClick(group: Group, product: Product)

    fun onProductSwipeStart(product: Product)

    fun onProductSwipePerform(group: Group, product: Product)

    fun onProductSwipeFinish(group: Group, product: Product)

    fun onProductSwipeCancel(product: Product)

    fun onProductEditClick(product: Product)

    fun onProductDragSucceed(fromGroup: Group, fromPosition: Int, toGroup: Group, toPosition: Int)
}

interface GroupClickHandler {
    fun onGroupClick(group: Group)

    fun onGroupSwipeStart(group: Group)

    fun onGroupSwipePerform(group: Group)

    fun onGroupSwipeFinish(group: Group)

    fun onGroupSwipeCancel(group: Group)

    fun onGroupEditClick(group: Group)

    fun onGroupDragSucceed(fromPosition: Int, toPosition: Int)

    fun onGroupCreateProductClick(group: Group)
}

interface GroupStorage {
    fun getGroupList(): List<Group>
}

const val VIEW_TYPE_ALWAYS_ON_TOP: Int = 1
const val VIEW_TYPE_STANDARD_GROUP: Int = 2

open class BaseViewHolder(view: View) :
    AbstractDraggableSwipeableItemViewHolder(view), ExpandableItemViewHolder {

    private val container = view.findViewById<View>(R.id.container)
    private val expandState = ExpandableItemState()

    override fun getSwipeableContainerView(): View = container

    override fun getExpandState() = expandState

    override fun getExpandStateFlags(): Int = expandState.flags

    override fun setExpandStateFlags(flags: Int) {
        expandState.flags = flags
    }
}

abstract class BaseGroupViewHolder(rootView: View) : BaseViewHolder(rootView) {
    abstract fun bindData(
        group: Group,
        handler: GroupClickHandler,
        expandManager: RecyclerViewExpandableItemManager,
        isAlwaysOnTopPresent: Boolean,
        positionInFilteredList: Int
    )
}

class GroupViewHolder(private val groupBinding: RvGroupElementBinding) :
    BaseGroupViewHolder(groupBinding.root) {

    override fun bindData(
        group: Group,
        handler: GroupClickHandler,
        expandManager: RecyclerViewExpandableItemManager,
        isAlwaysOnTopPresent: Boolean,
        positionInFilteredList: Int
    ) {
        groupBinding.group = group
        groupBinding.groupHandler = handler
        groupBinding.expandManager = expandManager
        groupBinding.isAlwayOnTopPresent = isAlwaysOnTopPresent
        groupBinding.positionInFilteredList = positionInFilteredList
        groupBinding.executePendingBindings()

    }
}

class UnsortedGroupViewHolder(private val groupBinding: RvUnsortedGroupElementBinding) :
    BaseGroupViewHolder(groupBinding.root) {

    override fun bindData(
        group: Group,
        handler: GroupClickHandler,
        expandManager: RecyclerViewExpandableItemManager,
        isAlwaysOnTopPresent: Boolean,
        positionInFilteredList: Int
    ) {
        groupBinding.group = group
        groupBinding.groupHandler = handler
        groupBinding.expandManager = expandManager
        groupBinding.positionInFilteredList = positionInFilteredList
        groupBinding.executePendingBindings()

    }
}

class ProductViewHolder(private val productBinding: RvProductElementBinding) :
    BaseViewHolder(productBinding.root) {

    fun bindData(group: Group, product: Product, handler: ProductClickHandler) {
        productBinding.parentGroup = group
        productBinding.product = product
        productBinding.productHandler = handler
        productBinding.executePendingBindings()
    }
}

@FragmentScope
class GroupsAndProductsAdapter @Inject constructor(
    private val groupClickHandler: GroupClickHandler,
    private val productClickHandler: ProductClickHandler,
    private val groupStorage: GroupStorage,
    private val expandManager: RecyclerViewExpandableItemManager
) : AbstractExpandableItemAdapter<BaseGroupViewHolder, ProductViewHolder>(),
    ExpandableDraggableItemAdapter<BaseGroupViewHolder, ProductViewHolder>,
    ExpandableSwipeableItemAdapter<BaseGroupViewHolder, ProductViewHolder> {

    private val TAG = "ProductAdapter"
    private val groupList: List<Group>
        get() = groupStorage.getGroupList()

    init {
        setHasStableIds(true)
    }

    override fun getChildCount(groupPosition: Int): Int {
        Log.d(TAG,
            "getChildCount, groupName: ${groupList[groupPosition].name}, size: ${groupList[groupPosition].productList.size}")
        return groupList[groupPosition].productList.size
    }

    override fun getGroupItemViewType(groupPosition: Int): Int =
        when (groupList[groupPosition].groupType) {
            GroupType.ALWAYS_ON_TOP -> VIEW_TYPE_ALWAYS_ON_TOP
            else -> VIEW_TYPE_STANDARD_GROUP
        }

    override fun onCheckCanExpandOrCollapseGroup(
        holder: BaseGroupViewHolder,
        groupPosition: Int,
        x: Int,
        y: Int,
        expand: Boolean
    ): Boolean = false // Проверка будет реализована вне этого метода, поэтому всегда false

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): BaseGroupViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_ALWAYS_ON_TOP) {
            val groupBinding =
                RvUnsortedGroupElementBinding.inflate(layoutInflater, parent, false)
            UnsortedGroupViewHolder(groupBinding)
        } else {
            val groupBinding = RvGroupElementBinding.inflate(layoutInflater, parent
                , false)
            GroupViewHolder(groupBinding)
        }
    }

    override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val productBinding = RvProductElementBinding.inflate(layoutInflater, parent, false)
        return ProductViewHolder(productBinding)
    }

    override fun getGroupId(groupPosition: Int): Long {
        Log.d(TAG,
            "getGroupId, LIST-POSITION = $groupPosition, DB-POSITION = ${groupList[groupPosition].position}  NAME = ${groupList[groupPosition].name}  ID = ${groupList[groupPosition].id} ")
        return groupList[groupPosition].id
    }

    override fun onBindChildViewHolder(
        holder: ProductViewHolder,
        groupPosition: Int,
        childPosition: Int,
        viewType: Int
    ) {
        val group =
            groupList[groupPosition]
        val product = group.productList[childPosition]

        holder.bindData(group,
            product,
            productClickHandler)
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        Log.d(TAG,
            "getChildId: groupName: ${groupList[groupPosition].name}, childPos: $childPosition")
        return groupList[groupPosition].productList[childPosition].id
    }

    override fun getGroupCount(): Int = groupList.size

    override fun onBindGroupViewHolder(
        holder: BaseGroupViewHolder,
        groupPosition: Int,
        viewType: Int
    ) {
        holder.bindData(groupList[groupPosition],
            groupClickHandler,
            expandManager,
            groupList[0].groupType == GroupType.ALWAYS_ON_TOP,
            groupPosition)
    }


    override fun onMoveGroupItem(fromGroupPosition: Int, toGroupPosition: Int) {
        // позиции списка и свойства конкретной группы могут различаться, т.к. список может быть предварительно отфильтрован
        groupClickHandler.onGroupDragSucceed(groupList[fromGroupPosition].position,
            groupList[toGroupPosition].position)
    }

    override fun onMoveChildItem(
        fromGroupPosition: Int,
        fromChildPosition: Int,
        toGroupPosition: Int,
        toChildPosition: Int
    ) {
        val fromProductList = groupList[fromGroupPosition].productList
        val draggingProduct = fromProductList[fromChildPosition]

        val toProductList = groupList[toGroupPosition].productList
        val dropProduct = when {
            toProductList.isEmpty() -> null
            toChildPosition > toProductList.lastIndex -> toProductList.last()
            else -> toProductList[toChildPosition]
        }

        val finalDropPosition =
            when {
                dropProduct == null -> 0
                draggingProduct.buyStatus == BuyStatus.NOT_BOUGHT -> {
                    if (dropProduct.buyStatus == BuyStatus.NOT_BOUGHT) toChildPosition
                    else {
                        when {
                            toProductList.none { it.buyStatus == BuyStatus.NOT_BOUGHT } -> 0
                            fromProductList == toProductList -> toProductList.indexOfLast { it.buyStatus == BuyStatus.NOT_BOUGHT }
                            else -> toProductList.indexOfFirst { it.buyStatus == BuyStatus.BOUGHT }
                        }
                    }
                }
                else -> {
                    if (dropProduct.buyStatus == BuyStatus.BOUGHT) toChildPosition
                    else {
                        if (toProductList.none { it.buyStatus == BuyStatus.BOUGHT }) toProductList.lastIndex + 1
                        else toProductList.indexOfFirst { it.buyStatus == BuyStatus.BOUGHT }
                    }
                }
            }

        productClickHandler.onProductDragSucceed(groupList[fromGroupPosition],
            fromChildPosition,
            groupList[toGroupPosition],
            finalDropPosition)
    }

    override fun onCheckGroupCanStartDrag(
        holder: BaseGroupViewHolder,
        groupPosition: Int,
        x: Int,
        y: Int
    ): Boolean =
    // если в фильтрованном списке есть (видна) дефолтная группа, то можно тащить все кроме нее,
        // если не видна, то тащить можно все группы
        if (groupList[0].groupType == GroupType.ALWAYS_ON_TOP) groupPosition > 0 else true

    override fun onCheckGroupCanDrop(draggingGroupPosition: Int, dropGroupPosition: Int): Boolean =
        true // на данный момент не используется, для использования нужно включить checkCanDrop(true)

    override fun onCheckChildCanDrop(
        draggingGroupPosition: Int,
        draggingChildPosition: Int,
        dropGroupPosition: Int,
        dropChildPosition: Int
    ): Boolean = true  // на данный момент не используется


    override fun onGetGroupItemDraggableRange(
        holder: BaseGroupViewHolder,
        groupPosition: Int
    ): ItemDraggableRange {
        val isAlwaysOnTopGroupPresent = groupList[0].groupType == GroupType.ALWAYS_ON_TOP
        return if (groupList[groupPosition].buyStatus == BuyStatus.NOT_BOUGHT) {
            val firstBoughtGroup =
                // не учитываем ALWAYS ON TOP группу
                groupList.find { it.groupType != GroupType.ALWAYS_ON_TOP && it.buyStatus == BuyStatus.BOUGHT }
            val firstBoughtGroupPosition =
                if (firstBoughtGroup != null) groupList.indexOf(firstBoughtGroup) - 1 else groupCount - 1
            ItemDraggableRange(if (isAlwaysOnTopGroupPresent) 1 else 0, firstBoughtGroupPosition)
        } else {
            val lastNotBoughtGroup =
                groupList.findLast { it.groupType != GroupType.ALWAYS_ON_TOP && it.buyStatus == BuyStatus.NOT_BOUGHT }
            val lastNotBoughtGroupPosition =
                if (lastNotBoughtGroup != null) groupList.indexOf(lastNotBoughtGroup) + 1 else {
                    if (isAlwaysOnTopGroupPresent) 1 else 0
                }
            ItemDraggableRange(lastNotBoughtGroupPosition,
                groupCount - 1)
        }
    }

    override fun onCheckChildCanStartDrag(
        holder: ProductViewHolder,
        groupPosition: Int,
        childPosition: Int,
        x: Int,
        y: Int
    ): Boolean = true

    override fun onGetChildItemDraggableRange(
        holder: ProductViewHolder,
        groupPosition: Int,
        childPosition: Int
    ): ItemDraggableRange? = null // любой диапазон

    override fun onGroupDragStarted(groupPosition: Int) {
        // не совсем понятно, зачем в примере разработчика фрейморвка тут стоит вызов notifyDataSetChanged()
        // отключение не влияет на функционал и анимации
        // notifyDataSetChanged()
    }

    override fun onGroupDragFinished(
        fromGroupPosition: Int,
        toGroupPosition: Int,
        result: Boolean
    ) {
        // а вот тут уже влияет на анимацию после перетаскивания
        notifyDataSetChanged()
    }

    override fun onChildDragStarted(groupPosition: Int, childPosition: Int) {
//        notifyDataSetChanged()
    }

    override fun onChildDragFinished(
        fromGroupPosition: Int,
        fromChildPosition: Int,
        toGroupPosition: Int,
        toChildPosition: Int,
        result: Boolean
    ) {
        notifyDataSetChanged()
    }


    override fun onSwipeGroupItem(
        holder: BaseGroupViewHolder,
        groupPosition: Int,
        result: Int
    ): SwipeResultAction? = when (result) {
        SwipeableItemConstants.RESULT_SWIPED_LEFT,
        SwipeableItemConstants.RESULT_SWIPED_RIGHT -> GroupSwipeDeleteResult(groupList[groupPosition])
        SwipeableItemConstants.RESULT_CANCELED -> {
            groupClickHandler.onGroupSwipeCancel(groupList[groupPosition])
            null
        }
        else -> throw UnsupportedOperationException("Unsupported swipe type")
    }

    override fun onSwipeGroupItemStarted(holder: BaseGroupViewHolder, groupPosition: Int) {
        groupClickHandler.onGroupSwipeStart(groupList[groupPosition])
    }

    override fun onGetGroupItemSwipeReactionType(
        holder: BaseGroupViewHolder,
        groupPosition: Int,
        x: Int,
        y: Int
    ): Int =
        if (groupList[groupPosition].groupType == GroupType.ALWAYS_ON_TOP) SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_ANY else SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H

    override fun onGetChildItemSwipeReactionType(
        holder: ProductViewHolder,
        groupPosition: Int,
        childPosition: Int,
        x: Int,
        y: Int
    ): Int = SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H

    override fun onSetGroupItemSwipeBackground(
        holder: BaseGroupViewHolder,
        groupPosition: Int,
        type: Int
    ) {
        holder.itemView.setBackgroundResource(getGroupSwipeBackground(type))
    }

    private fun getGroupSwipeBackground(type: Int) = when (type) {
        SwipeableItemConstants.DRAWABLE_SWIPE_LEFT_BACKGROUND -> R.drawable.bg_swipe_group_left
        SwipeableItemConstants.DRAWABLE_SWIPE_RIGHT_BACKGROUND -> R.drawable.bg_swipe_group_right
        else -> R.color.swipeBackgroundTransparent
    }

    override fun onSetChildItemSwipeBackground(
        holder: ProductViewHolder,
        groupPosition: Int,
        childPosition: Int,
        type: Int
    ) {
        holder.itemView.setBackgroundResource(getChildSwipeBackground(type))
    }

    private fun getChildSwipeBackground(type: Int) = when (type) {
        SwipeableItemConstants.DRAWABLE_SWIPE_LEFT_BACKGROUND -> R.drawable.bg_swipe_product_left
        SwipeableItemConstants.DRAWABLE_SWIPE_RIGHT_BACKGROUND -> R.drawable.bg_swipe_product_right
        else -> R.color.swipeBackgroundTransparent
    }

    override fun onSwipeChildItemStarted(
        holder: ProductViewHolder,
        groupPosition: Int,
        childPosition: Int
    ) {
        productClickHandler.onProductSwipeStart(groupList[groupPosition].productList[childPosition])
    }

    override fun onSwipeChildItem(
        holder: ProductViewHolder,
        groupPosition: Int,
        childPosition: Int,
        result: Int
    ): SwipeResultAction? = when (result) {
        SwipeableItemConstants.RESULT_SWIPED_LEFT,
        SwipeableItemConstants.RESULT_SWIPED_RIGHT -> ProductSwipeDeleteResult(groupList[groupPosition],
            groupList[groupPosition].productList[childPosition])
        SwipeableItemConstants.RESULT_CANCELED -> {
            productClickHandler.onProductSwipeCancel(groupList[groupPosition].productList[childPosition])
            null
        }
        else -> throw UnsupportedOperationException("Unsupported swipe type")
    }

    private inner class GroupSwipeDeleteResult(val group: Group) : SwipeResultActionRemoveItem() {

        override fun onPerformAction() {
            groupClickHandler.onGroupSwipePerform(group)
        }

        override fun onSlideAnimationEnd() {
            groupClickHandler.onGroupSwipeFinish(group)
        }
    }

    private inner class ProductSwipeDeleteResult(val group: Group, val product: Product) :
        SwipeResultActionRemoveItem() {

        override fun onPerformAction() {
            productClickHandler.onProductSwipePerform(group, product)
        }

        override fun onSlideAnimationEnd() {
            productClickHandler.onProductSwipeFinish(group, product)
        }
    }
}