package br.com.gabrielmorais.terminalgertec

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.VisibleForTesting
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Suppress("DEPRECATION")
object UiUtils {
  fun hideSystemUi(window: Window, blockInterface: Boolean = false) {
    if (blockInterface) {
      window.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
      )
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      WindowCompat.setDecorFitsSystemWindows(window, false)
      WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
          WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      }
    } else {
      window.decorView.systemUiVisibility = (
          View.SYSTEM_UI_FLAG_IMMERSIVE
              or View.SYSTEM_UI_FLAG_FULLSCREEN
              or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
              or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
              or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
              or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
          )
    }
  }

  @VisibleForTesting
  fun isValidHexColor(color: String): Boolean {
    return color.matches("^#([0-9A-Fa-f]{3}){1,2}".toRegex())
  }

}
