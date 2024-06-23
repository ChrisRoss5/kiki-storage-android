package dev.k1k1.kikistorage

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.GravityCompat
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.k1k1.kikistorage.databinding.ActivityMainBinding
import dev.k1k1.kikistorage.framework.startActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        initHamburgerMenu()
        initNavigation()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity<SignInActivity>();
            finish()
            return
        }
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
            signOut()
            true
        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                toggleDrawer()
                return true
            }

            R.id.menuExit -> {
                exitApp()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun exitApp() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.exit)
            setMessage(getString(R.string.really_exit_the_application))
            setIcon(R.drawable.baseline_exit_to_app_24)
            setCancelable(true)
            setNegativeButton(getString(R.string.cancel), null)
            setPositiveButton("OK") { _, _ -> finish() }
            show()
        }
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawers()
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun signOut() {
        AuthUI.getInstance().signOut(this)
        startActivity<SignInActivity>()
        finish()
    }
}