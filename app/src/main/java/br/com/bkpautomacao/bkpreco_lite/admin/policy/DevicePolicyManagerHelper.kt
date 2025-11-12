package br.com.bkpautomacao.bkpreco_lite.admin.policy

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.bkpautomacao.bkpreco_lite.R
import br.com.bkpautomacao.bkpreco_lite.admin.AdminReceiver
import java.io.File

class DevicePolicyManagerHelper(private val context: Context) {
  private val dpm: DevicePolicyManager = context
    .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
  private val adminComponentName = ComponentName(
    context.applicationContext, AdminReceiver::class.java
  )

  fun isAdmin() = dpm.isDeviceOwnerApp(context.packageName)

  fun enableLockTask(activity: AppCompatActivity) {
    if (dpm.isDeviceOwnerApp(context.packageName)) {
      dpm.setLockTaskPackages(adminComponentName, arrayOf(context.packageName))
      activity.startLockTask()
      Toast.makeText(context, "Kiosk Mode Ativado", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "App não é o device owner", Toast.LENGTH_SHORT).show()
    }
  }

  fun stopLockTask(activity: AppCompatActivity) {
    activity.stopLockTask()
    Toast.makeText(context, "kiosk Mode Desativado", Toast.LENGTH_SHORT).show()
  }

  fun installApp(apk: File) {
    Log.i("DevicePolicyManager", "Criando sessão de instalação")
    val packageInstaller = context.packageManager.packageInstaller
    val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
    val sessionId = packageInstaller.createSession(params)
    val session = packageInstaller.openSession(sessionId)
    Log.i("DevicePolicyManager", "Sessão de instalação aberta")

    apk.inputStream().use { input ->
      session.openWrite(context.packageName, 0, apk.length()).use { output ->
        val buffer = ByteArray(65536)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
          output.write(buffer, 0, bytesRead)
        }
        session.fsync(output)
      }
    }

    session.commit(createIntentSenderForSession(sessionId))
    session.close()

    Log.i("DevicePolicyManager", "Instalação finalizada")
    apk.delete()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      dpm.reboot(adminComponentName)
    }

  }

  private fun createIntentSenderForSession(sessionId: Int): IntentSender {
    val intent = Intent(context.getString(R.string.ACTION_INSTALL_APP))

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      sessionId,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    return pendingIntent.intentSender
  }

}