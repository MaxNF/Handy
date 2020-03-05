package ru.netfantazii.handy.core.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.core.main.NetworkViewModel
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.notifications.alarm.AlarmFragment
import ru.netfantazii.handy.core.notifications.map.GoogleMapFragment
import ru.netfantazii.handy.extensions.getRequiredMapPermissions
import java.lang.UnsupportedOperationException

class NotificationTabFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback {
    private val TAG = "NotificationTabFragment"
    private lateinit var notificationTabAdapter: NotificationTabAdapter
    private val fragmentArgs: NotificationTabFragmentArgs by navArgs()
    private var mapPermissionGranted = false
    private lateinit var tabLayout: TabLayout
    private lateinit var networkViewModel: NetworkViewModel

    private val savedTabIndexBundleKey = "tab_index"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkViewModel = ViewModelProviders.of(activity!!).get(NetworkViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.notification_tab_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        tabLayout = view.findViewById(R.id.tab_layout)
        val viewPager = view.findViewById<ViewPager2>(R.id.viewpager)
        notificationTabAdapter = NotificationTabAdapter(this,
            fragmentArgs.catalogId,
            fragmentArgs.catalogName,
            fragmentArgs.groupExpandStates)
        viewPager.adapter = notificationTabAdapter
        viewPager.isUserInputEnabled = false

        savedInstanceState?.let {
            val selectedTab = it.getInt(savedTabIndexBundleKey, 0)
            tabLayout.selectTab(tabLayout.getTabAt(selectedTab))
        }

        tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    when (tab.position) {
                        0 -> viewPager.currentItem = 0
                        1 -> {
                            if (networkViewModel.user.get() == null) {
                                LoginFirstDialog().show(childFragmentManager, "login_first_dialog")
                                tabLayout.selectTab(tabLayout.getTabAt(0))
                                return
                            }
                            checkPermissions()
                            if (mapPermissionGranted) {
                                viewPager.currentItem = 1
                            } else {
                                tabLayout.selectTab(tabLayout.getTabAt(0))
                            }
                        }
                        else -> throw NoSuchElementException("Unknown tab position")
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
            })

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(savedTabIndexBundleKey, tabLayout.selectedTabPosition)
    }

    private fun checkPermissions() {
        val anyPermissionNotGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) checkPermissionsForApi29plus() else
                checkPermissionsForApi21plus()

        if (anyPermissionNotGranted) {
            requestPermissions(getRequiredMapPermissions(), 0)
        } else {
            mapPermissionGranted = true
        }
    }

    private fun isPermissionNotGranted(permission: String) =
        ContextCompat.checkSelfPermission(context!!,
            permission) != PackageManager.PERMISSION_GRANTED

    @SuppressLint("InlinedApi")
    private fun checkPermissionsForApi29plus() =
        isPermissionNotGranted(Manifest.permission.INTERNET) ||
                isPermissionNotGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
                isPermissionNotGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    private fun checkPermissionsForApi21plus() =
        isPermissionNotGranted(Manifest.permission.INTERNET) ||
                isPermissionNotGranted(Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isEmpty() || grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
            GrantPermissionsManuallyDialog().show(childFragmentManager, "permission_dialog")
        } else {
            mapPermissionGranted = true
            tabLayout.selectTab(tabLayout.getTabAt(1))
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
            1 -> GoogleMapFragment().apply { this.arguments = arguments }
            else -> throw UnsupportedOperationException("No fragment for position #$position")
        }
    }
}

class GrantPermissionsManuallyDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity, R.style.BaseDialogTheme)
            .setMessage(R.string.grant_perm_manually_message)
            .setNegativeButton(R.string.dialog_ok_button, null)
            .create()
    }
}
class LoginFirstDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity, R.style.BaseDialogTheme)
            .setMessage(R.string.login_required_dialog_message)
            .setNegativeButton(R.string.dialog_ok_button, null)
            .create()
    }
}