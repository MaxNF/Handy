package ru.netfantazii.handy.core.catalogs

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

interface CatalogsClickHandler {
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

}

interface CatalogsStorage {
    fun getCatalogList(): List<Catalog>
}

class CatalogsViewHolder(private val catalogBinding: RvCatalogElementBinding) :
    AbstractDraggableSwipeableItemViewHolder(catalogBinding.root) {

    val container = catalogBinding.root.findViewById<View>(R.id.container)
    val draghandle = catalogBinding.root.findViewById<View>(R.id.drag_handle)

    override fun getSwipeableContainerView(): View = container

    fun bindData(catalog: Catalog, handler: CatalogsClickHandler) {
        catalogBinding.catalog = catalog
        catalogBinding.handler = handler
        catalogBinding.executePendingBindings() //В ресайклервью нужно сразу связать данные!
    }


}

class CatalogsAdapter(
    private val catalogsClickHandler: CatalogsClickHandler,
    private val catalogsStorage: CatalogsStorage
) :
    RecyclerView.Adapter<CatalogsViewHolder>(),
    DraggableItemAdapter<CatalogsViewHolder>, SwipeableItemAdapter<CatalogsViewHolder> {

    private val TAG = "CatalogsAdapter"
    private val catalogList: List<Catalog>
        get() = catalogsStorage.getCatalogList()

    init {
        setHasStableIds(true)
    }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val catalogBinding = RvCatalogElementBinding.inflate(layoutInflater, parent, false)
        return CatalogsViewHolder(catalogBinding)
    }

    override fun getItemCount(): Int {
        return catalogList.size
    }

    override fun getItemId(position: Int): Long {
        Log.d(TAG, "getItemId: ${catalogList[position].id}")
        return catalogList[position].id
    }

    override fun onBindViewHolder(holder: CatalogsViewHolder, position: Int) {
        holder.bindData(catalogList[position], catalogsClickHandler)
    }

    override fun onGetItemDraggableRange(
        holder: CatalogsViewHolder,
        position: Int
    ): ItemDraggableRange? {
        return null
    }

    override fun onCheckCanStartDrag(
        holder: CatalogsViewHolder,
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
        catalogsClickHandler.onCatalogDragSucceed(fromPosition, toPosition)
    }

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean {
        return true
    }

    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
//        notifyDataSetChanged()
    }

    override fun onSwipeItem(
        holder: CatalogsViewHolder,
        position: Int,
        result: Int
    ): SwipeResultAction? {
        return when (result) {
            SwipeableItemConstants.RESULT_SWIPED_RIGHT,
            SwipeableItemConstants.RESULT_SWIPED_LEFT -> SwipeDeleteResult(catalogList[position])
            SwipeableItemConstants.RESULT_CANCELED -> {
                catalogsClickHandler.onCatalogSwipeCancel(catalogList[position])
                null
            }
            else -> null
        }
    }

    override fun onGetSwipeReactionType(
        holder: CatalogsViewHolder,
        position: Int,
        x: Int,
        y: Int
    ): Int {
        return SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H
    }

    override fun onSwipeItemStarted(holder: CatalogsViewHolder, position: Int) {
//        notifyDataSetChanged()
        catalogsClickHandler.onCatalogSwipeStart(catalogList[position])

    }

    override fun onSetSwipeBackground(holder: CatalogsViewHolder, position: Int, type: Int) {
        val backgroundResourceId = when (type) {
            SwipeableItemConstants.DRAWABLE_SWIPE_LEFT_BACKGROUND -> R.drawable.bg_swipe_item_left
            SwipeableItemConstants.DRAWABLE_SWIPE_RIGHT_BACKGROUND -> R.drawable.bg_swipe_item_right
            else -> R.color.swipeBackgroundTransparent
        }
        holder.itemView.setBackgroundResource(backgroundResourceId)
    }

    private inner class SwipeDeleteResult(val catalog: Catalog) : SwipeResultActionRemoveItem() {

        override fun onPerformAction() {
            catalogsClickHandler.onCatalogSwipePerform(catalog)
        }

        override fun onSlideAnimationEnd() {
            catalogsClickHandler.onCatalogSwipeFinish(catalog)
        }
    }
}

