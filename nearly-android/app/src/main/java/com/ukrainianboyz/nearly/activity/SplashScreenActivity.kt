package com.ukrainianboyz.nearly.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.ukrainianboyz.nearly.ApplicationPrefs
import com.ukrainianboyz.nearly.utils.json.JsonUtils
import com.ukrainianboyz.nearly.utils.preferences.stringLiveData


//Activity to be used during start
class SplashScreenActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        if(!checkGooglePlayServices())finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userJson = ApplicationPrefs.encryptedPrefs.stringLiveData(ApplicationPrefs.USER_KEY,"").value
        val intent = if(userJson.isNullOrEmpty()){
            Intent(this, LoginActivity::class.java)
        }else {
            val user = JsonUtils.jsonToUser(userJson)
            if(user.appUserId.isNullOrBlank()){
                Intent(this, LoginActivity::class.java) // activity will re-navigate to needed fragment
            }else{
                Intent(this, MainActivity::class.java)
            }
        }
        startActivity(intent)
        finish()
    }
    private fun checkGooglePlayServices(): Boolean {
        val availability = GoogleApiAvailability.getInstance()
        val resultCode = availability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            val dialog = availability.getErrorDialog(this, resultCode, 0)
            dialog.show()
            return false
        }
        return true
    }
}
