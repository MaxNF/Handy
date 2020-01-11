package ru.netfantazii.handy.core.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import ru.netfantazii.handy.R

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.welcome_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val beginButton = view.findViewById<Button>(R.id.begin_button)
        val navController = NavHostFragment.findNavController(this)
        beginButton.setOnClickListener { navController.popBackStack() }
    }
}