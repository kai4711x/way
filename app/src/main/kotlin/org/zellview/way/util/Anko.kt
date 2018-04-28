package org.zellview.way.util

import android.content.Context
import android.support.design.widget.Snackbar
import android.util.TypedValue
import android.view.View
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.resources

//inline fun AnkoContext<*>.string(id: Int): String = resources.getString(id)

fun Context.attrAsDimen(value : Int) : Int{
  return TypedValue.complexToDimensionPixelSize(attribute(value).data, getResources().getDisplayMetrics())
}

fun Context.attribute(value : Int) : TypedValue {
  var ret = TypedValue()
  theme.resolveAttribute(value, ret, true)

  return ret
}

/*

fun View.snackbar(text: CharSequence, duration: Int = Snackbar.LENGTH_SHORT, init: Snackbar.() -> Unit = {}): Snackbar {
  val snack = Snackbar.make(this, text, duration)
  snack.init()
  snack.show()
  return snack
}

fun View.snackbar(text: Int, duration: Int = Snackbar.LENGTH_SHORT, init: Snackbar.() -> Unit = {}): Snackbar {
  val snack = Snackbar.make(this, text, duration)
  snack.init()
  snack.show()
  return snack
}
*/