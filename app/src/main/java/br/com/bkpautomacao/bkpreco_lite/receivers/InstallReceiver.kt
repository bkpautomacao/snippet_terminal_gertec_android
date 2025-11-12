package br.com.bkpautomacao.bkpreco_lite.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import br.com.bkpautomacao.bkpreco_lite.R
import br.com.bkpautomacao.bkpreco_lite.ui.MainActivity

class InstallReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent?.action == context?.getString(R.string.ACTION_INSTALL_APP)) {
      Log.i("InstallReceiver", "Reinicializando app depois da atualização!")
      // Reiniciar o aplicativo
      val bootIntent = Intent(context, MainActivity::class.java)
      bootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context?.startActivity(bootIntent)
    }
  }
}