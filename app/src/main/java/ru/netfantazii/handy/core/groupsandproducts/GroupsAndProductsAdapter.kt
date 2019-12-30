package ru.netfantazii.handy.core.groupsandproducts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableDraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemState
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemViewHolder
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableSwipeableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import ru.netfantazii.handy.R
import ru.netfantazii.handy.databinding.RvGroupElementBinding
import ru.netfantazii.handy.databinding.RvProductElementBinding
import ru.netfantazii.handy.databinding.RvUnsortedGroupElementBinding
import ru.netfantazii.handy.db.Group
import ru.netfantazii.handy.db.Product
import java.lang.UnsupportedOperationException

interface ProductClickHandler {
    fun onProductClick(product: Product)

    fun onProductSwipeStart(product: Product)

    fun onProductSwipePerform(group: Group, product: Product)

    fun onProductSwipeFinish(product: Product)

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
    abstract fun bindData(group: Group, handler: GroupClickHandler)
}

class GroupViewHolder(private val groupBinding: RvGroupElementBinding) :
    BaseGroupViewHolder(groupBinding.root) {

    override fun bindData(group: Group, handler: GroupClickHandler) {
        groupBinding.group = group
        groupBinding.groupHandler = handler
        groupBinding.executePendingBindings()
    }
}

class UnsortedGroupViewHolder(private val groupBinding: RvUnsortedGroupElementBinding) :
    BaseGroupViewHolder(groupBinding.root) {

    override fun bindData(group: Group, handler: GroupClickHandler) {
        groupBinding.group = group
        groupBinding.executePendingBindings()
    }
}

class ProductViewHolder(private val productBinding: RvProductElementBinding) :
    BaseViewHolder(productBinding.root) {

    fun bindData(product: Product, handler: ProductClickHandler) {
        productBinding.product = product
        productBinding.productHandler = handler
        productBinding.executePendingBindings()
    }
}

class GroupsAndProductsAdapter(
    private val groupClickHandler: GroupClickHandler,
    private val productClickHandler: ProductClickHandler,
    private val groupStorage: GroupStorage
) : AbstractExpandableItemAdapter<BaseGroupViewHolder, ProductViewHolder>(),
    ExpandableDraggableItemAdapter<BaseGroupViewHolder, ProductViewHolder>,
    ExpandableSwipeableItemAdapter<BaseGroupViewHolder, ProductViewHolder> {

    private val viewTypeAlwaysOnTop = 1
    private val viewTypeStandardGroup = 2

    private val TAG = "ProductAdapter"
    private val groupList: List<Group>
        get() = groupStorage.getGroupList()

    init {
        setHasStableIds(true)
    }

    override fun getChildCount(groupPosition: Int): Int = groupList[groupPosition].productList.size

    override fun getGroupItemViewType(groupPosition: Int): Int {
        return if (groupPosition == 0) viewTypeAlwaysOnTop else viewTypeStandardGroup
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
        return if (viewType == viewTypeAlwaysOnTop) {
            val groupBinding = RvUnsortedGroupElementBinding.inflate(layoutInflater, parent, false)
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
        holder.bindData(groupList[groupPosition].productList[childPosition], productClickHandler)
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long =
        groupList[groupPosition].productList[childPosition].id

    override fun getGroupCount(): Int = groupList.size

    override fun onBindGroupViewHolder(
        holder: BaseGroupViewHolder,
        groupPosition: Int,
        viewType: Int
    ) {
        holder.bindData(groupList[groupPosition], groupClickHandler)
    }

    override fun onGroupDragFinished(
        fromGroupPosition: Int,
        toGroupPosition: Int,
        result: Boolean
    ) {
        // no action
    }

    override fun onMoveGroupItem(fromGroupPosition: Int, toGroupPosition: Int) {
        groupClickHandler.onGroupDragSucceed(fromGroupPosition, toGroupPosition)
    }

    override fun onCheckGroupCanDrop(draggingGroupPosition: Int, dropGroupPosition: Int): Boolean =
        dropGroupPosition > 0

    override fun onMoveChildItem(
        fromGroupPosition: Int,
        fromChildPosition: Int,
        toGroupPosition: Int,
        toChildPosition: Int
    ) {
        productClickHandler.onProductDragSucceed(fromGroupPosition,
            fromChildPosition,
            toGroupPosition,
            toChildPosition)
    }

    override fun onCheckGroupCanStartDrag(
        holder: BaseGroupViewHolder,
        groupPosition: Int,
        x: Int,
        y: Int
    ): Boolean = groupPosition > 0

    override fun onCheckChildCanDrop(
        draggingGroupPosition: Int,
        draggingChildPosition: Int,
        dropGroupPosition: Int,
        dropChildPosition: Int
    ): Boolean = true

    override fun onGetGroupItemDraggableRange(
        holder: BaseGroupViewHolder,
        groupPosition: Int
    ): ItemDraggableRange = ItemDraggableRange(1, groupCount - 1)

    override fun onChildDragStarted(groupPosition: Int, childPosition: Int) {
        // no action
    }

    override fun onGetChildItemDraggableRange(
        holder: ProductViewHolder,
        groupPosition: Int,
        childPosition: Int
    ): ItemDraggableRange? = null // любой диапазон

    override fun onChildDragFinished(
        fromGroupPosition: Int,
        fromChildPosition: Int,
        toGroupPosition: Int,
        toChildPosition: Int,
        result: Boolean
    ) {
        // no action
    }

    override fun onGroupDragStarted(groupPosition: Int) {
        // no action
    }

    override fun onCheckChildCanStartDrag(
        holder: ProductViewHolder,
        groupPosition: Int,
        childPosition: Int,
        x: Int,
        y: Int
    ): Boolean = true

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
        // no action
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
        // no action
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
            productClickHandler.onProductSwipeFinish(product)
        }
    }
}