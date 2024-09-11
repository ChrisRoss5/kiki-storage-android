package dev.k1k1.kikistorage

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.GravityCompat
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import dev.k1k1.kikistorage.databinding.ActivityMainBinding
import dev.k1k1.kikistorage.firebase.Auth
import dev.k1k1.kikistorage.framework.startActivity
import dev.k1k1.kikistorage.util.DialogUtil

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initHamburgerMenu()
        initNavigation()
    }

    override fun onStart() {
        super.onStart()
        if (Auth.auth.currentUser == null) {
            startActivity<SignInActivity>()
            finish()
            return
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                toggleDrawer()
                return true
            }

            R.id.menuExit -> {
                DialogUtil.showExitAppDialog(this) { finish() }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.exit_menu, menu)
        val exitMenuItem = menu?.findItem(R.id.menuExit)
        val exitIconDrawable = exitMenuItem?.icon
        exitIconDrawable?.let {
            it.mutate()
            it.setTint(ContextCompat.getColor(this, R.color.white))
        }
        return true
    }

    private fun initHamburgerMenu() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val menuIconDrawable = ContextCompat.getDrawable(this, R.drawable.baseline_menu_24)
        menuIconDrawable?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            Color.WHITE,
            BlendModeCompat.SRC_ATOP
        )
        supportActionBar?.setHomeAsUpIndicator(menuIconDrawable)
    }

    private fun initNavigation() {
        val navController = Navigation.findNavController(this, R.id.navController)
        NavigationUI.setupWithNavController(binding.navView, navController)
        val signOutMenuItem = binding.navView.menu.findItem(R.id.menuSignOut)
        signOutMenuItem?.setOnMenuItemClickListener {
            Auth.signOut(this)
            true
        }
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawers()
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }
}