package com.hotukrainianboyz.nearly.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.hotukrainianboyz.nearly.R
import com.hotukrainianboyz.nearly.ApplicationPrefs
import com.hotukrainianboyz.nearly.utils.json.JsonUtils
import com.hotukrainianboyz.nearly.utils.preferences.stringLiveData


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        val navController = findNavController(R.id.login_nav_host)
        val prefs = ApplicationPrefs.encryptedPrefs
        val userLiveData = prefs.stringLiveData(ApplicationPrefs.USER_KEY,"")
        userLiveData.observe(this, Observer {
            if(!it.isNullOrEmpty()){
                val userObj = JsonUtils.jsonToUser(userLiveData.value!!)
                if(!userObj.appUserId.isNullOrBlank()){
                    navController.navigate(R.id.mainActivity)
                    finish()
                }else{
                    navController.popBackStack()
                    navController.navigate(R.id.createAppUserIdFragment)
                }
            }
        })
    }

}
