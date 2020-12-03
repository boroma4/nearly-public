package com.ukrainianboyz.nearly.activity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.ukrainianboyz.nearly.ApplicationPrefs
import com.ukrainianboyz.nearly.R
import com.ukrainianboyz.nearly.advertisement.AdsManager
import com.ukrainianboyz.nearly.entity.User
import com.ukrainianboyz.nearly.service.signalR.SignalRClient
import com.ukrainianboyz.nearly.utils.apiUtils.FriendRequestStatus
import com.ukrainianboyz.nearly.utils.image.ImageUtils
import com.ukrainianboyz.nearly.utils.json.JsonUtils
import com.ukrainianboyz.nearly.utils.layout.RoundedBackgroundSpan
import com.ukrainianboyz.nearly.utils.preferences.SharedPreferenceLiveData
import com.ukrainianboyz.nearly.utils.preferences.stringLiveData
import com.ukrainianboyz.nearly.viewModel.FriendsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val navController: NavController by lazy { findNavController(R.id.main_nav_host) }
    private val navigationView by lazy { findViewById<NavigationView>(R.id.nav_view) }
    private val toolbar by lazy { findViewById<Toolbar>(R.id.main_toolbar) }
    private val friendsViewModel: FriendsViewModel by viewModels()
    private lateinit var switchThemeButton: ImageButton
    private lateinit var userAvatarView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var appUserIdTextView: TextView
    private lateinit var friendRequestsMenuItem: MenuItem
    private lateinit var standardFriendRequestsTitle: String
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var userLiveData: SharedPreferenceLiveData<String>
    private lateinit var adsManager: AdsManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initActivity()

        userLiveData.observe(this, Observer { userJson ->
            if (!userJson.isNullOrBlank()) {
                lifecycleScope.launch(Dispatchers.Default) {
                    val user = JsonUtils.jsonToUser(userJson)
                    launch(Dispatchers.Main) {
                        updateDrawer(user)
                    }
                }
            }
        })

        friendsViewModel.friends.observe(this, Observer { friends ->
            val requests =
                friends.filter { friend -> friend.friendRequestStatus == FriendRequestStatus.INCOMING }
            if (requests.isEmpty()) {
                friendRequestsMenuItem.title = standardFriendRequestsTitle
            } else {
                val s = "$standardFriendRequestsTitle    ${requests.size} "
                val sColored = SpannableString(s)
                sColored.setSpan(
                    RoundedBackgroundSpan(Color.RED, Color.WHITE),
                    s.length - 3,
                    s.length,
                    0
                )
                friendRequestsMenuItem.title = sColored
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // if app was in the background and connection was down, first we fetch friends and then reconnect
        if(SignalRClient.isOnline.value == false)
            friendsViewModel.fetchFriendsOnly()

        lifecycleScope.launch(Dispatchers.IO) {
            userLiveData.value?.let {
                val id = JsonUtils.jsonToUser(it).id
                // will not reconnect if user is already connected
                SignalRClient.connectToHub(applicationContext, id)
            }
        }
    }

    //changing stuff on drawer should go here
    @SuppressLint("SetTextI18n")
    private fun updateDrawer(user: User) {
        ImageUtils.loadCircularImage(user.imageUrl, userAvatarView)
        userNameTextView.text = user.name
        appUserIdTextView.text = "ID: " + user.appUserId
    }

    private fun initActivity() {
        adsManager = AdsManager(this)
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        toolbar.setupWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)

        navigationView.menu.findItem(R.id.support_ad_menu_item).setOnMenuItemClickListener {
            if (!adsManager.showInterstitialAd()) Toast.makeText(
                this,
                getString(R.string.operation_failed),
                Toast.LENGTH_LONG
            ).show()
            true
        }

        val drawerHeader = navigationView.getHeaderView(0)
        userAvatarView = drawerHeader.findViewById(R.id.drawer_header_avatar)
        switchThemeButton = drawerHeader.findViewById(R.id.switch_theme_btn)
        userNameTextView = drawerHeader.findViewById(R.id.drawer_header_username)
        appUserIdTextView = drawerHeader.findViewById(R.id.drawer_header_app_user_id)
        switchThemeButton.setImageResource(if (isDarkMode()) R.drawable.ic_wb_sunny_black_24dp else R.drawable.ic_moon_black_24dp)

        friendRequestsMenuItem = navigationView.menu.findItem(R.id.friendRequestsFragment)
        standardFriendRequestsTitle = friendRequestsMenuItem.title as String


        val prefs = ApplicationPrefs.encryptedPrefs
        userLiveData = prefs.stringLiveData(ApplicationPrefs.USER_KEY, "")

        switchThemeButton.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(if (isDarkMode()) MODE_NIGHT_NO else MODE_NIGHT_YES)
        }
    }

    private fun isDarkMode(): Boolean {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            else -> false
        }
    }
}
