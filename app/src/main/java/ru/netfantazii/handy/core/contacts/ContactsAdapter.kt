package ru.netfantazii.handy.core.contacts

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.netfantazii.handy.model.Contact

interface ContactsClickHandler {
    fun onContactDeleteClick()
    fun onContactEditClick()
}

interface ContactsStorage {
    fun getContacts(): List<Contact>
}

class ContactsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind() {

    }
}

class ContactsAdapter : RecyclerView.Adapter<ContactsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}