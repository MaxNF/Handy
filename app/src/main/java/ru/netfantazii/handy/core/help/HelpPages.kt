package ru.netfantazii.handy.core.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.netfantazii.handy.R

const val HELP_PAGE_COUNT = 5

open class BaseHelpPage(private val pageLayoutResId: Int) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(pageLayoutResId, container, false)
    }
}

class HelpPage1 : BaseHelpPage(R.layout.help_page_01)
class HelpPage2 : BaseHelpPage(R.layout.help_page_02)
class HelpPage3 : BaseHelpPage(R.layout.help_page_03)
class HelpPage4 : BaseHelpPage(R.layout.help_page_04)
class HelpPage5 : BaseHelpPage(R.layout.help_page_05)