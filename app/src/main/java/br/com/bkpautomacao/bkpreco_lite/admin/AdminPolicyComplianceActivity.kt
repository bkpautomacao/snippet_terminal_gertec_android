package br.com.bkpautomacao.bkpreco_lite.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.bkpautomacao.bkpreco_lite.R

class AdminPolicyComplianceActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_admin_policy_compliance)
    val intent = intent
    setResult(RESULT_OK, intent)
    finish()
  }
}