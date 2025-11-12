package br.com.bkpautomacao.bkpreco_lite.admin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import br.com.bkpautomacao.bkpreco_lite.R
import br.com.bkpautomacao.bkpreco_lite.ui.MainActivity


class AdminReceiver : DeviceAdminReceiver() {

  override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
    super.onProfileProvisioningComplete(context, intent)
    val manager = context
      .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    val componentName = ComponentName(context.applicationContext, DeviceAdminReceiver::class.java)
    manager.setProfileName(componentName, context.getString(R.string.profile_name))

    val openMainActivity = Intent(context, MainActivity::class.java)
      .apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    context.startActivity(openMainActivity)
  }
}