package ru.netfantazii.handy.core.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.notifications.alarm.AlarmFragment
import ru.netfantazii.handy.core.notifications.map.MapFragment
import java.lang.UnsupportedOperationException

class NotificationTabFragment : Fragment() {
    private lateinit var notificationTabAdapter: NotificationTabAdapter
    private val fragmentArgs: NotificationTabFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.notification_tab_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewPager = view.findViewById<ViewPager2>(R.id.viewpager)
        notificationTabAdapter = NotificationTabAdapter(this,
            fragmentArgs.catalogId,
            fragmentArgs.catalogName,
            fragmentArgs.groupExpandStates)
        viewPager.adapter = notificationTabAdapter
        viewPager.isUserInputEnabled = false

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.setCurrentItem(tab?.position ?: 0, true)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

    }
}

class NotificationTabAdapter(
    fragment: Fragment,
    private val catalogId: Long,
    private val catalogName: String,
    private val expandStates: RecyclerViewExpandableItemManager.SavedState
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        val arguments = Bundle()
        with(arguments) {
            putLong(BUNDLE_CATALOG_ID_KEY, catalogId)
            putString(BUNDLE_CATALOG_NAME_KEY, catalogName)
            putParcelable(BUNDLE_EXPAND_STATE_KEY, expandStates)
        }
        return when (position) {
            0 -> AlarmFragment().apply { this.arguments = arguments }
            1 -> MapFragment().apply { this.arguments = arguments }
            else -> throw UnsupportedOperationException("No fragment for position #$position")
        }
    }
}