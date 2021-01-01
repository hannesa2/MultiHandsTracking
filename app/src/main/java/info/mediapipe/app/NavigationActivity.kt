package info.mediapipe.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import info.hannes.github.AppUpdateHelper
import info.hannes.logcat.LogcatActivity
import info.mediapipe.app.holistic.HolisticActivity
import info.mediapipe.app.multihand.MultiHandActivity

abstract class NavigationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val headerLayout = navigationView.getHeaderView(0)
        val textVersion = headerLayout.findViewById<TextView>(R.id.textVersion)
        textVersion.text = BuildConfig.VERSION
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        if (id == R.id.nav_multihand) {
            openActivity(MultiHandActivity::class.java)
        } else if (id == R.id.nav_holistic) {
            openActivity(HolisticActivity::class.java)
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun openActivity(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
        if (clazz.isInstance(NavigationActivity::class.java)) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_update -> {
                AppUpdateHelper.checkForNewVersion(
                    this,

                    BuildConfig.GIT_REPOSITORY,
                    BuildConfig.VERSION_NAME
                )
                true
            }
            R.id.action_logcat -> {
                openActivity(LogcatActivity::class.java)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}