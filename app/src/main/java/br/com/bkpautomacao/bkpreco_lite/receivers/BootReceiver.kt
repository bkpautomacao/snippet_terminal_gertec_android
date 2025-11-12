package br.com.bkpautomacao.bkpreco_lite.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.bkpautomacao.bkpreco_lite.ui.MainActivity

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    val bootIntent = Intent(context, MainActivity::class.java)
    bootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context?.startActivity(bootIntent)
  }
}