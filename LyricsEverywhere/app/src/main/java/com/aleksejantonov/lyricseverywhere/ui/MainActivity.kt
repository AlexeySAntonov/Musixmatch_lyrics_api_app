package com.aleksejantonov.lyricseverywhere.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MenuItem
import com.aleksejantonov.lyricseverywhere.*
import com.aleksejantonov.lyricseverywhere.sl.SL
import com.aleksejantonov.lyricseverywhere.ui.base.BaseFragment
import com.aleksejantonov.lyricseverywhere.ui.base.QueryType
import com.aleksejantonov.lyricseverywhere.ui.base.QueryType.*
import com.aleksejantonov.lyricseverywhere.ui.base.ScreenType
import com.aleksejantonov.lyricseverywhere.ui.base.ScreenType.*
import com.aleksejantonov.lyricseverywhere.ui.base.ScreenType.SETTINGS
import com.aleksejantonov.lyricseverywhere.ui.login.LoginActivity
import kotlinx.android.synthetic.main.activity_main.drawerLayout
import kotlinx.android.synthetic.main.activity_main.navigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
  companion object {
    private const val DRAWER_ITEM_ID = "drawer_item_id"
    fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
  }

  private var lastItem: MenuItem? = null
  private var lastItemId = R.id.rus

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    savedInstanceState?.let { lastItemId = it[DRAWER_ITEM_ID] as Int }
    defaultInit(lastItemId)

    navigationView.apply {
      setNavigationItemSelectedListener(this@MainActivity)
      itemIconTintList = null
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(DRAWER_ITEM_ID, lastItemId)
  }

  override fun onBackPressed() {
    if (drawerLayout.isDrawerOpen(Gravity.START)) drawerLayout.closeDrawer(Gravity.START)
    else super.onBackPressed()
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item?.itemId) {
      android.R.id.home -> supportFinishAfterTransition()
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onNavigationItemSelected(item: MenuItem): Boolean {
    selectDrawerItem(item)
    return true
  }

  fun toggleDrawer() {
    if (drawerLayout.isDrawerOpen(Gravity.START)) drawerLayout.closeDrawer(Gravity.START)
    else drawerLayout.openDrawer(Gravity.START)
  }

  private fun defaultInit(lastItemId: Int?) =
      supportFragmentManager
          .findFragmentById(R.id.fragmentContainer)?.let {
            lastItemId?.let {
              lastItem = navigationView.menu.findItem(it)
              if (it != R.id.settings && it != R.id.logOut && it != R.id.advancedSearch) lastItem?.setIcon(R.drawable.ic_star_gold_24dp)
            }
          }
          ?: onNavigationItemSelected(navigationView.menu.getItem(0))

  private fun selectDrawerItem(item: MenuItem) {
    lastItem?.let {
      if (it.itemId != R.id.settings && it.itemId != R.id.advancedSearch) it.setIcon(R.drawable.ic_star_white_24dp)
    }

    item.apply {
      lastItem = this
      lastItemId = itemId
    }

    val country = when (item.itemId) {
      R.id.usa -> US
      R.id.gb  -> GB
      else     -> RU
    }

    when (item.itemId) {
      R.id.advancedSearch -> navigateTo(ADVANCED_SEARCH)
      R.id.settings       -> navigateTo(SETTINGS)
      R.id.logOut         -> {
        SL.componentManager().appComponent.preferences.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
      }
    }

    if (item.itemId != R.id.logOut && item.itemId != R.id.settings && item.itemId != R.id.advancedSearch) {
      navigateTo(CHART, country)
      item.setIcon(R.drawable.ic_star_gold_24dp)
    }
    drawerLayout.closeDrawers()
  }

  fun navigateTo(screen: ScreenType, queryType: QueryType = RU, addToBackStack: Boolean = false, animate: Boolean = false) {
    supportFragmentManager.beginTransaction()
        .apply { if (animate) setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right) }
        .replace(R.id.fragmentContainer, BaseFragment.newInstance(screen, queryType))
        .apply { if (addToBackStack) addToBackStack(null) }
        .commitAllowingStateLoss()
  }
}
