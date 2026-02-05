package com.madinaappstudio.recallmate.main

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.homeNavHost) as NavHostFragment

        navController = navHostFragment.navController

        binding.bottomNavHome.setupWithNavController(navController)

        val topLevelDestinations = listOf(
            R.id.dashboardFragment,
//            R.id.uploadFragment,
            R.id.libraryFragment,
            R.id.settingsFragment,
        )

        onBackPressedDispatcher.addCallback(this) {
            if (!navController.navigateUp()) {
                finish()
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevel = destination.hierarchy.any {
                it.id in topLevelDestinations
            }

            binding.bottomNavHome.visibility = if (isTopLevel) View.VISIBLE else View.GONE
        }

        binding.bottomNavHome.setOnItemReselectedListener { item ->
            navController.popBackStack(item.itemId, false)
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}