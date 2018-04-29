package org.zellview.way.view

import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.rareventure.gps2.R
//import com.vsouhrada.android.kotlin.anko.drawer.R.attr.theme
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.*
import org.jetbrains.anko.support.v4._DrawerLayout
import org.jetbrains.anko.support.v4.drawerLayout
import org.zellview.way.activity.MapsActivity
//import org.zellview.way.R
import org.zellview.way.util.attrAsDimen

//import org.zellview.way.R

class MapsActivityView : AnkoComponent<MapsActivity> {

    override fun createView(ui: AnkoContext<MapsActivity>): View {
        return with(ui) {
            drawerLayout {
                id = R.id.drawer_layout;
                fitsSystemWindows = true;
                lparams(
                        width = matchParent,
                        height = matchParent
                )
                createAppBar(ui)
                createNavigationView(ui)
            }
        }
    }


    fun _DrawerLayout.createAppBar(ui: AnkoContext<MapsActivity>) {

        coordinatorLayout {

            fitsSystemWindows = false;
            lparams(width = matchParent, height = matchParent);

            // https://github.com/Kotlin/anko/issues/210
            include<FrameLayout>(R.layout.content_maps)

            toolbar {

                id = R.id.toolbar

                //popupTheme = R.style.AppTheme_PopupOverlay;
                popupTheme = R.style.AppTheme_AppBarOverlay
                backgroundResource = R.color.colorPrimary
                lparams(width = matchParent, height = ui.ctx.attrAsDimen(R.attr.actionBarSize))
            }

            appBarLayout() {
                id = R.id.app_bar_main

                lparams(
                        width = matchParent,
                        height = wrapContent
                        //theme = R.style.AppTheme_AppBarOverlay
                )
            }

            floatingActionButton {
                id = R.id.fab
                imageResource = R.drawable.ic_startrecording
                backgroundColor = ContextCompat.getColor(ui.owner, R.color.colorAccent)
                //onClick { }
                        /*
                        Snackbar.make(this, "Replace with your own action", Snackbar.LENGTH_LONG)
                       .setAction("Action",null).show()
                        ui.toast("Clicked Snack")
                        */
            }.lparams {
                margin = resources.getDimensionPixelSize(R.dimen.fab_margin)
                gravity = Gravity.BOTTOM or GravityCompat.END
            }
        }.lparams {
                width = matchParent;
                height = matchParent
        }
    }

      /*
      onClick {
        //  snackbar("Replace with your own action", Snackbar.LENGTH_LONG) {
        //    setAction("Action") { ui.toast("Clicked Snack") }
        //  }
        }
      }
      */

    fun _DrawerLayout.createNavigationView(ui: AnkoContext<MapsActivity>) {
        navigationView {
            id = R.id.nav_view
            fitsSystemWindows = true
            lparams(height = matchParent, gravity = GravityCompat.START)
            setNavigationItemSelectedListener(ui.owner)
            inflateHeaderView(R.layout.nav_header_main)
            inflateMenu(R.menu.activity_main_drawer)
        }
    }
}