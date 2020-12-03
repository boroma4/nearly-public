package com.ukrainianboyz.nearly.utils.login

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task

data class GoogleLoginResult(val token: String?, val error: String?)

class GoogleLoginUtils{
    companion object{
        //Google error code
        private const val SIGN_IN_CANCELLED = 12501

        fun handleSignInResult(completedTask: Task<GoogleSignInAccount>):GoogleLoginResult {
            return try {
                val account = completedTask.getResult(ApiException::class.java)
                account?.let {
                    GoogleLoginResult(it.idToken,null)
                } ?: run{ throw ApiException(Status.RESULT_INTERNAL_ERROR)}
            } catch (e: ApiException) {
                Log.e("Google login", e.statusCode.toString() + GoogleSignInStatusCodes.getStatusCodeString(e.statusCode));
                GoogleLoginResult(null,if(e.statusCode == SIGN_IN_CANCELLED) null else "Login failed")
            }
        }
        fun createGoogleLoginClient(serverId: String, context: Context): GoogleSignInClient{
            val gso = buildSignInOptions(serverId)
            return GoogleSignIn.getClient(context,gso)
        }
        private fun buildSignInOptions(serverId: String) : GoogleSignInOptions {
            return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverId)
                .requestEmail()
                .build()
        }
    }
}