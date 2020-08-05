package com.hotukrainianboyz.nearly.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hotukrainianboyz.nearly.R
import com.hotukrainianboyz.nearly.repository.UserRepository
import com.hotukrainianboyz.nearly.service.webAPI.BackendService
import com.hotukrainianboyz.nearly.service.webAPI.IBackendService
import com.hotukrainianboyz.nearly.utils.apiUtils.NetworkRequest
import com.hotukrainianboyz.nearly.utils.apiUtils.RequestStatus
import com.hotukrainianboyz.nearly.utils.login.GoogleLoginUtils
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.hotukrainianboyz.nearly.dao.UserDao
import com.hotukrainianboyz.nearly.database.AppRoomDatabase
import com.hotukrainianboyz.nearly.service.signalR.SignalRClient
import com.hotukrainianboyz.nearly.utils.apiUtils.NetworkRequestHelper
import com.hotukrainianboyz.nearly.ApplicationPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val applicationContext: Context = application.applicationContext
    private val backendService : IBackendService = BackendService().backendApi
    private val userDao = UserDao(ApplicationPrefs.encryptedPrefs)
    private val networkRequestHelper = NetworkRequestHelper(viewModelScope,TAG,applicationContext)
    private val repository: UserRepository
    val mGoogleSignInClient : GoogleSignInClient
    var request: MutableLiveData<NetworkRequest>
    private set

    init {
        val friendsDao = AppRoomDatabase.getDatabase(applicationContext).friendsDao()
        repository = UserRepository(friendsDao,userDao,backendService)
        request = MutableLiveData(NetworkRequest(RequestStatus.UNDEFINED))
        mGoogleSignInClient = GoogleLoginUtils.createGoogleLoginClient(application.getString(R.string.google_server_client_id),applicationContext)
    }


    fun login(token: String){
        networkRequestHelper.process(request){
            repository.login(token)
        }
    }

    fun logout() = viewModelScope.launch(Dispatchers.IO)  {
        launch(Dispatchers.Default) {repository.logout()}
        mGoogleSignInClient.signOut()
        SignalRClient.disconnectFromHub(true)
    }

    fun updateAppUserId(appUserId: String){
        networkRequestHelper.process(request){
            repository.updateAppUserId(appUserId)
        }
    }

    fun refreshRequestLiveData(){
        request = MutableLiveData(NetworkRequest(RequestStatus.UNDEFINED))
    }

    companion object{
        private const val TAG = "UserViewModel"
    }
}