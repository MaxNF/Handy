package ru.netfantazii.handy.core.groupsandproducts

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
import ru.netfantazii.handy.db.BuyStatus
import ru.netfantazii.handy.db.Group
import ru.netfantazii.handy.db.Product
import java.lang.UnsupportedOperationException

interface ProductClickHandler {
    fun onProductClick(group: Group, product: Product)

    fun onProductSwipeStart(product: Product)

    fun onProductSwipePerform(group: Group, product: Product)

    fun onProductSwipeFinish(group: Group, product: Product)

    fun onProductSwipeCancel(product: Product)

    fun onProductEditClick(product: Product)

    fun onProductDragSucceed(fromGroup: Int, fromPosition: Int, toGroup: Int, toPosition: Int)
}

interface GroupClickHandler {
    fun onGroupClick(groupPosition: Int)

    fun onGroupSwipeStart(group: Group)

    fun onGroupSwipePerform(group: Group)

    fun onGroupSwipeFinish(group: Group)

    fun onGroupSwipeCancel(group: Group)

    fun onGroupEditClick(group: Group)

    fun onGroupDragSucceed(fromPosition: Int, toPosition: Int)

    fun onGroupCreateProductClick(groupId: Long)
}

interface GroupStorage {
    fun getGroupList(): List<Group>
}

const val VIEW_TYPE_ALWAYS_ON_TOP = 1
const val VIEW_TYPE_STANDARD_GROUP = 2
const val VIEW_TYPE_FIRST_GROUP = 3

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
        expandManager: RecyclerViewExpandableItemManager
    )
}

class GroupViewHolder(private val groupBinding: RvGroupElementBinding) :
    BaseGroupViewHolder(groupBinding.root) {

    override fun bindData(
        group: Group,
        handler: GroupClickHandler,
        expandManager: RecyclerViewExpandableItemManager
    ) {
        groupBinding.group = group
        groupBinding.groupHandler = handler
        groupBinding.executePendingBindings()
        groupBinding.expandManager = expandManager
    }
}

class UnsortedGroupViewHolder(private val groupBinding: RvUnsortedGroupElementBinding) :
    BaseGroupViewHolder(groupBinding.root) {

    override fun bindData(
        group: Group,
        handler: GroupClickHandler,
        expandManager: RecyclerViewExpandableItemManager
    ) {
        groupBinding.group = group
        groupBinding.groupHandler = handler
        groupBinding.executePendingBindings()
        groupBinding.expandManager = expandManager
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

class GroupsAndProductsAdapter(
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

    override fun getChildCount(groupPosition: Int): Int = groupList[groupPosition].productList.size

    override fun getGroupItemViewType(groupPosition: Int): Int =
        if (groupPosition == 0) VIEW_TYPE_ALWAYS_ON_TOP else VIEW_TYPE_STANDARD_GROUP

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

    override fun getGroupId(groupPosition: Int): Long = groupList[groupPosition].id

    override fun onBindChildViewHolder(
        holder: ProductViewHolder,
        groupPosition: Int,
        childPosition: Int,
        viewType: Int
    ) {
        holder.bindData(groupList[groupPosition],
            groupList[groupPosition].productList[childPosition],
            productClickHandler)
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long =
        groupList[groupPosition].productList[childPosition].id

    override fun getGroupCount(): Int = groupList.size

    override fun onBindGroupViewHolder(
        holder: BaseGroupViewHolder,
        groupPosition: Int,
        viewType: Int
    ) {
        holder.bindData(groupList[groupPosition], groupClickHandler, expandManager)
    }


    override fun onMoveGroupItem(fromGroupPosition: Int, toGroupPosition: Int) {
        groupClickHandler.onGroupDragSucceed(fromGroupPosition, toGroupPosition)
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

        productClickHandler.onProductDragSucceed(fromGroupPosition,
            fromChildPosition,
            toGroupPosition,
            finalDropPosition)
    }

    override fun onCheckGroupCanStartDrag(
        holder: BaseGroupViewHolder,
        groupPosition: Int,
        x: Int,
        y: Int
    ): Boolean = groupPosition > 0

    override fun onCheckGroupCanDrop(draggingGroupPosition: Int, dropGroupPosition: Int): Boolean {
        // на данный момент не используется, для использования нужно включить checkCanDrop(true)
//        val notTopGroup = dropGroupPosition > 0
//        val notBoughtGroup = groupList[dropGroupPosition].buyStatus != BuyStatus.BOUGHT
//        return notTopGroup && notBoughtGroup
        return true
    }

    override fun onCheckChildCanDrop(
        draggingGroupPosition: Int,
        draggingChildPosition: Int,
        dropGroupPosition: Int,
        dropChildPosition: Int
    ): Boolean {
//        val draggingProductList = groupList[draggingGroupPosition].productList
//        val draggingProduct = draggingProductList[draggingChildPosition]
//
//        val dropProductList =
//            if (dropGroupPosition > groupList.lastIndex) groupList.last().productList else groupList[dropGroupPosition].productList
//        val dropProduct =
//            if (dropChildPosition > dropProductList.lastIndex) dropProductList.last() else dropProductList[dropChildPosition]
//
//        return if (draggingProduct.buyStatus == BuyStatus.NOT_BOUGHT) {
//            if (dropProductList.all { it.buyStatus == BuyStatus.BOUGHT } && dropChildPosition == 0) {
//                true
//            } else {
//                dropProduct.buyStatus == BuyStatus.NOT_BOUGHT
//            }
//        } else {
//            if (dropProductList.all { it.buyStatus == BuyStatus.NOT_BOUGHT } && dropChildPosition == dropProductList.size) {
//                true
//            } else {
//                dropProduct.buyStatus == BuyStatus.BOUGHT
//            }
//        }
        return true
    } // на данный момент не используется

    override fun onGetGroupItemDraggableRange(
        holder: BaseGroupViewHolder,
        groupPosition: Int
    ): ItemDraggableRange {
        return if (groupList[groupPosition].buyStatus == BuyStatus.NOT_BOUGHT) {
            val firstBoughtGroup =
                groupList.subList(1, groupList.size)
                    .find { it.buyStatus == BuyStatus.BOUGHT } // не учитываем ALWAYS ON TOP группу
            val firstBoughtGroupPosition =
                if (firstBoughtGroup != null) firstBoughtGroup.position - 1 else groupCount - 1
            ItemDraggableRange(1, firstBoughtGroupPosition)
        } else {
            val lastNotBoughtGroup = groupList.findLast { it.buyStatus == BuyStatus.NOT_BOUGHT }
            val lastNotBoughtGroupPosition =
                if (lastNotBoughtGroup != null) lastNotBoughtGroup.position + 1 else 1
            ItemDraggableRange(lastNotBoughtGroupPosition, groupCount - 1)
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
        notifyDataSetChanged()
    }

    override fun onGroupDragFinished(
        fromGroupPosition: Int,
        toGroupPosition: Int,
        result: Boolean
    ) {
        notifyDataSetChanged()
    }

    override fun onChildDragStarted(groupPosition: Int, childPosition: Int) {
        notifyDataSetChanged()
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

    override fun onSetGroupItemSwipeBackground(
        holder: BaseGroupViewHolder,
        groupPosition: Int,
        type: Int
    ) {
        holder.itemView.setBackgroundResource(getOnSwipeBackground(type))
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
        if (groupPosition > 0) SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H else SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_ANY

    override fun onGetChildItemSwipeReactionType(
        holder: ProductViewHolder,
        groupPosition: Int,
        childPosition: Int,
        x: Int,
        y: Int
    ): Int = SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H

    override fun onSetChildItemSwipeBackground(
        holder: ProductViewHolder,
        groupPosition: Int,
        childPosition: Int,
        type: Int
    ) {
        holder.itemView.setBackgroundResource(getOnSwipeBackground(type))
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

    private fun getOnSwipeBackground(type: Int) = when (type) {
        SwipeableItemConstants.DRAWABLE_SWIPE_LEFT_BACKGROUND -> R.drawable.bg_swipe_item_left
        SwipeableItemConstants.DRAWABLE_SWIPE_RIGHT_BACKGROUND -> R.drawable.bg_swipe_item_right
        else -> R.color.swipeBackgroundTransparent
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