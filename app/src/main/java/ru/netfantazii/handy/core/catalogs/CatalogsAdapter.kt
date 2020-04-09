package ru.netfantazii.handy.core.catalogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.main.NetworkViewModel
import ru.netfantazii.handy.databinding.RvCatalogElementBinding
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.data.User
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.di.UnwrappedAdapter
import java.lang.UnsupportedOperationException
import javax.inject.Inject

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

    /**
     * Вызывается при клике на значок "поделиться" у каталога*/
    fun onCatalogShareClick(catalog: Catalog)

    fun onCatalogEnvelopeClick(catalog: Catalog)
}

interface CatalogStorage {
    fun getCatalogList(): List<Catalog>
}

class CatalogViewHolder(private val catalogBinding: RvCatalogElementBinding) :
    AbstractDraggableSwipeableItemViewHolder(catalogBinding.root) {

    private val container = catalogBinding.container
    val draghandle = catalogBinding.dragHandle

    override fun getSwipeableContainerView(): View = container

    fun bindData(
        catalog: Catalog,
        handler: CatalogClickHandler,
        user: ObservableField<User?>
    ) {
        catalogBinding.catalog = catalog
        catalogBinding.handler = handler
        catalogBinding.user = user
        catalogBinding.executePendingBindings() //В ресайклервью нужно сразу связать данные!
    }
}

@FragmentScope
class CatalogsAdapter @Inject constructor(
    private val catalogClickHandler: CatalogClickHandler,
    private val catalogStorage: CatalogStorage,
    private val networkViewModel: NetworkViewModel
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
        holder.bindData(catalogList[position], catalogClickHandler, networkViewModel.user)
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
        catalogClickHandler.onCatalogDragSucceed(catalogList[fromPosition].position,
            catalogList[toPosition].position)
    }

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean {
        return true
    }

    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
        notifyDataSetChanged()
    }

    override fun onSwipeItem(
        holder: CatalogViewHolder,
        position: Int,
        result: Int
    ): SwipeResultAction? = when (result) {
        SwipeableItemConstants.RESULT_SWIPED_RIGHT,
        SwipeableItemConstants.RESULT_SWIPED_LEFT -> SwipeDeleteResult(catalogList[position])
        SwipeableItemConstants.RESULT_CANCELED -> {
            catalogClickHandler.onCatalogSwipeCancel(catalogList[position])
            null
        }
        else -> throw UnsupportedOperationException("Unsupported swipe type")
    }

    override fun onGetSwipeReactionType(
        holder: CatalogViewHolder,
        position: Int,
        x: Int,
        y: Int
    ): Int = SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H


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

