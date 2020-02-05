package ru.netfantazii.handy.core.contacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder
import ru.netfantazii.handy.R
import ru.netfantazii.handy.databinding.RvContactElementBinding
import ru.netfantazii.handy.model.Contact
import java.lang.UnsupportedOperationException

interface ContactsClickHandler {
    fun onContactSwipePerform(contact: Contact)
    fun onContactEditClick(contact: Contact)
}

interface ContactsStorage {
    fun getContacts(): List<Contact>
}

class ContactsViewHolder(private val binding: RvContactElementBinding) :
    AbstractSwipeableItemViewHolder(binding.root) {
    private val container: View = binding.container

    override fun getSwipeableContainerView(): View = container

    fun bind(contact: Contact) {
        binding.contact = contact
        binding.executePendingBindings()
    }
}

class ContactsAdapter(
    private val clickHandler: ContactsClickHandler,
    private val contactsStorage: ContactsStorage
) : RecyclerView.Adapter<ContactsViewHolder>(),
    SwipeableItemAdapter<ContactsViewHolder> {

    private val TAG = "ContactsAdapter"
    private val contacts: List<Contact>
        get() = contactsStorage.getContacts()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RvContactElementBinding.inflate(layoutInflater, parent, false)
        return ContactsViewHolder(binding)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun onSwipeItem(
        holder: ContactsViewHolder,
        position: Int,
        result: Int
    ): SwipeResultAction? =
        when (result) {
            SwipeableItemConstants.RESULT_SWIPED_RIGHT,
            SwipeableItemConstants.RESULT_SWIPED_LEFT -> SwipeDeleteResult(contacts[position])
            SwipeableItemConstants.RESULT_CANCELED -> {
                null
            }
            else -> throw UnsupportedOperationException("Unsupported swipe type")
        }

    override fun onGetSwipeReactionType(p0: ContactsViewHolder, p1: Int, p2: Int, p3: Int): Int =
        SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H

    override fun onSwipeItemStarted(p0: ContactsViewHolder, p1: Int) {
        // do nothing
    }

    override fun onSetSwipeBackground(holder: ContactsViewHolder, position: Int, type: Int) {
        val backgroundResourceId = when (type) {
            SwipeableItemConstants.DRAWABLE_SWIPE_LEFT_BACKGROUND -> R.drawable.bg_swipe_catalog_left
            SwipeableItemConstants.DRAWABLE_SWIPE_RIGHT_BACKGROUND -> R.drawable.bg_swipe_catalog_right
            else -> R.color.swipeBackgroundTransparent
        }
        holder.itemView.setBackgroundResource(backgroundResourceId)
    }

    private inner class SwipeDeleteResult(val contact: Contact) : SwipeResultActionRemoveItem() {

        override fun onPerformAction() {
            clickHandler.onContactSwipePerform(contact)
        }
    }
}