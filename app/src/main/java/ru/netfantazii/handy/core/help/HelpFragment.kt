package ru.netfantazii.handy.core.help

import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import ru.netfantazii.handy.core.main.MainActivity
import ru.netfantazii.handy.R
import java.lang.UnsupportedOperationException

class HelpFragment : Fragment() {
    var previousPage: Int = 0
    private val TAG = "HelpFragment"
    private val transitionDuration = 300 // milliseconds

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.help_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewPager = view.findViewById<ViewPager>(R.id.pager)
        viewPager.adapter = createFragmentPagerAdapter()
        setUpPageSelector(viewPager)
        (activity as MainActivity).checkMenuItem(R.id.navigation_help_fragment)
    }

    private fun setUpPageSelector(viewPager: ViewPager) {
        val selectorIcons = arrayOf<ImageView>(
            view!!.findViewById(R.id.help_selection_icon_1),
            view!!.findViewById(R.id.help_selection_icon_2),
            view!!.findViewById(R.id.help_selection_icon_3),
            view!!.findViewById(R.id.help_selection_icon_4),
            view!!.findViewById(R.id.help_selection_icon_5)
        )
        (selectorIcons[previousPage].drawable as TransitionDrawable).startTransition(
            transitionDuration)

        viewPager.addOnPageChangeListener( object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                (selectorIcons[previousPage].drawable as TransitionDrawable).reverseTransition(transitionDuration)
                (selectorIcons[position].drawable as TransitionDrawable).startTransition(transitionDuration)
                previousPage = position
            }
        })
    }

    private fun createFragmentPagerAdapter(): FragmentPagerAdapter =
        object : FragmentPagerAdapter(childFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return when (position) {
                    0 -> HelpPage1()
                    1 -> HelpPage2()
                    2 -> HelpPage3()
                    3 -> HelpPage4()
                    4 -> HelpPage5()
                    else -> throw UnsupportedOperationException("Can't find a fragment for the page#$position")
                }
            }
            override fun getCount() = HELP_PAGE_COUNT
        }
}