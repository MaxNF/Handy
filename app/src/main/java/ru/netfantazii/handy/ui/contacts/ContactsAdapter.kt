package ru.netfantazii.handy.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.netfantazii.handy.data.model.InputFilter
import ru.netfantazii.handy.databinding.RvContactElementBinding
import ru.netfantazii.handy.data.model.Contact

interface ContactsClickHandler {
    fun onContactDeleteClick(contact: Contact)
    fun onContactEditClick(contact: Contact)
}

interface ContactsStorage {
    fun getContacts(): List<Contact>
    fun getValidContacts(): List<Contact>
}

class ContactsViewHolder(private val binding: RvContactElementBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(contact: Contact, handler: ContactsClickHandler, inputFilter: InputFilter) {
        binding.handler = handler
        binding.contact = contact
        binding.inputFilter = inputFilter
        binding.executePendingBindings()
    }
}

class ContactsAdapter(
    private val clickHandler: ContactsClickHandler,
    private val contactsStorage: ContactsStorage,
    private val inputFilter: InputFilter
) : RecyclerView.Adapter<ContactsViewHolder>() {

    private val TAG = "ContactsAdapter"
    private val contacts: List<Contact>
        get() = contactsStorage.getContacts()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RvContactElementBinding.inflate(layoutInflater, parent, false)
        return ContactsViewHolder(binding)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.bind(contacts[position], clickHandler, inputFilter)
    }
}