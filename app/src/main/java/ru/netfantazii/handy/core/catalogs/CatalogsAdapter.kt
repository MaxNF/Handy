package ru.netfantazii.handy.core.catalogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder
import ru.netfantazii.handy.R
import ru.netfantazii.handy.databinding.RvCatalogElementBinding
import ru.netfantazii.handy.db.Catalog
import java.lang.UnsupportedOperationException

interface CatalogClickHandler {
    /**
     * Вызывается при клике на пустое место на каталоге. Необходимо открыть этот каталог при клике на него.*/
    fun onCatalogClick(catalog: Catalog)

    /**
     * Вызывается при попытке свайпа. Пользователь все еще может отменить свайп.*/
    fun onCatalogSwipeStart(catalog: Catalog)

    /**
     * Вызывается при начале анимации свайпа. Необходимо инициировать удаление каталога.*/
    fun onCatalogSwipePerform(catalog: Catalog)

    /**
     * Вызывается когда заканчивается анимация свайпа. Можно вызвать снэкбар с undo кнопкой.*/
    fun onCatalogSwipeFinish(catalog: Catalog)

    /**
     * Вызывается при отмене пользователем свайпа (свайп не был произведен до конца).*/
    fun onCatalogSwipeCancel(catalog: Catalog)

    /**
     * Вызывается при клике на значок карандаша у каталога*/
    fun onCatalogEditClick(catalog: Catalog)

    /**
     * Вызывается при успешном перетаскивании каталога.*/
    fun onCatalogDragSucceed(fromPosition: Int, toPosition: Int)

    /**
     * Вызывается при клике на значок колокольчика у каталога*/
    fun onCatalogNotificationClick(catalog: Catalog)

}

interface CatalogStorage {
    fun getCatalogList(): List<Catalog>
}

class CatalogViewHolder(private val catalogBinding: RvCatalogElementBinding) :
    AbstractDraggableSwipeableItemViewHolder(catalogBinding.root) {

    private val container = catalogBinding.root.findViewById<View>(R.id.container)
    val draghandle = catalogBinding.root.findViewById<View>(R.id.drag_handle)

    override fun getSwipeableContainerView(): View = container

    fun bindData(catalog: Catalog, handler: CatalogClickHandler) {
        catalogBinding.catalog = catalog
        catalogBinding.handler = handler
        catalogBinding.executePendingBindings() //В ресайклервью нужно сразу связать данные!
    }
}

class CatalogsAdapter(
    private val catalogClickHandler: CatalogClickHandler,
    private val catalogStorage: CatalogStorage
) :
    RecyclerView.Adapter<CatalogViewHolder>(),
    DraggableItemAdapter<CatalogViewHolder>, SwipeableItemAdapter<CatalogViewHolder> {

    private val TAG = "CatalogsAdapter"
    private val catalogList: List<Catalog>
        get() = catalogStorage.getCatalogList()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val catalogBinding = RvCatalogElementBinding.inflate(layoutInflater, parent, false)
        return CatalogViewHolder(catalogBinding)
    }

    override fun getItemCount(): Int = catalogList.size


    override fun getItemId(position: Int): Long = catalogList[position].id

    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        holder.bindData(catalogList[position], catalogClickHandler)
    }

    override fun onGetItemDraggableRange(
        holder: CatalogViewHolder,
        position: Int
    ): ItemDraggableRange? {
        return null
    }

    override fun onCheckCanStartDrag(
        holder: CatalogViewHolder,
        position: Int,
        x: Int,
        y: Int
    ): Boolean {
        return true
    }

    override fun onItemDragStarted(position: Int) {
//        notifyDataSetChanged()
    }

    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
        catalogClickHandler.onCatalogDragSucceed(fromPosition, toPosition)
    }

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean {
        return true
    }

    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
//        notifyDataSetChanged()
    }

    override fun onSwipeItem(
        holder: CatalogViewHolder,
        position: Int,
        result: Int
    ): SwipeResultAction? {
        return when (result) {
            SwipeableItemConstants.RESULT_SWIPED_RIGHT,
            SwipeableItemConstants.RESULT_SWIPED_LEFT -> SwipeDeleteResult(catalogList[position])
            SwipeableItemConstants.RESULT_CANCELED -> {
                catalogClickHandler.onCatalogSwipeCancel(catalogList[position])
                null
            }
            else -> throw UnsupportedOperationException("Unsupported swipe type")
        }
    }

    override fun onGetSwipeReactionType(
        holder: CatalogViewHolder,
        position: Int,
        x: Int,
        y: Int
    ): Int {
        return SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H
    }

    override fun onSwipeItemStarted(holder: CatalogViewHolder, position: Int) {
        catalogClickHandler.onCatalogSwipeStart(catalogList[position])
    }

    override fun onSetSwipeBackground(holder: CatalogViewHolder, position: Int, type: Int) {
        val backgroundResourceId = when (type) {
            SwipeableItemConstants.DRAWABLE_SWIPE_LEFT_BACKGROUND -> R.drawable.bg_swipe_catalog_left
            SwipeableItemConstants.DRAWABLE_SWIPE_RIGHT_BACKGROUND -> R.drawable.bg_swipe_catalog_right
            else -> R.color.swipeBackgroundTransparent
        }
        holder.itemView.setBackgroundResource(backgroundResourceId)
    }

    private inner class SwipeDeleteResult(val catalog: Catalog) : SwipeResultActionRemoveItem() {

        override fun onPerformAction() {
            catalogClickHandler.onCatalogSwipePerform(catalog)
        }

        override fun onSlideAnimationEnd() {
            catalogClickHandler.onCatalogSwipeFinish(catalog)
        }
    }
}

