package com.ukrainianboyz.nearly.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.SignInButton
import com.ukrainianboyz.nearly.R
import com.ukrainianboyz.nearly.utils.apiUtils.RequestStatus
import com.ukrainianboyz.nearly.utils.login.GoogleLoginUtils
import com.ukrainianboyz.nearly.viewModel.FriendsViewModel
import com.ukrainianboyz.nearly.viewModel.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    private lateinit var googleLoginButton: SignInButton
    private lateinit var loginProgressBar: ProgressBar
    private val userViewModel: UserViewModel by activityViewModels()
    private val friendsViewModel: FriendsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectViews(view)
        userViewModel.refreshRequestLiveData()

        userViewModel.request.observe(viewLifecycleOwner, Observer { request ->
            when (request.status) {
                RequestStatus.FAILED -> {
                    hideProgress()
                    onLoginFailed(request.error)
                }
                RequestStatus.PENDING -> {
                    showProgress()
                }
                RequestStatus.SUCCESS -> {
                    friendsViewModel.fetchFriendsAndRequests{
                        // global scope here because this activity will be destroyed right after executing this
                        GlobalScope.launch(Dispatchers.Main) {
                            Toast.makeText(requireActivity(),R.string.failed_to_load_friends,Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else ->{}
            }
        })

    }
    // do google stuff
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val result = GoogleLoginUtils.handleSignInResult(task)
            val token = result.token
            token?.let{
                userViewModel.login(token)
            } ?: run{
                // if no error and no token -> user cancelled
                result.error?.let{onLoginFailed(it)}
            }
        }
    }
    private fun connectViews(view: View){
        loginProgressBar = view.findViewById(R.id.login_progress_bar)
        googleLoginButton = view.findViewById(R.id.google_sign_in_button)
        googleLoginButton.setOnClickListener{
            startActivityForResult(userViewModel.mGoogleSignInClient.signInIntent, RC_SIGN_IN)
        }
    }
    private fun showProgress(){
        loginProgressBar.visibility = View.VISIBLE
        googleLoginButton.isEnabled = false
    }
    private fun hideProgress(){
        loginProgressBar.visibility = View.INVISIBLE
        googleLoginButton.isEnabled = true
    }
    private fun onLoginFailed(message: String){
        googleLoginButton.isEnabled = true
        Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show()
    }

    companion object{
        private const val RC_SIGN_IN = 9001
    }
}
