package com.ukrainianboyz.nearly.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.ukrainianboyz.nearly.R
import com.ukrainianboyz.nearly.utils.apiUtils.RequestStatus
import com.ukrainianboyz.nearly.viewModel.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateAppUserIdFragment : Fragment() {

    private lateinit var mTextInputLayout: TextInputLayout
    private lateinit var mSubmitButton: Button
    private lateinit var mBackToLoginButton: ExtendedFloatingActionButton
    private lateinit var mNavigator: NavController
    private var mUserInput = ""
    private val mUserViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_app_user_id, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mUserViewModel.refreshRequestLiveData()
        mNavigator = findNavController()
        connectViews(view)
        setupListeners()
        mUserViewModel.request.observe(viewLifecycleOwner, Observer {
            when(it.status){
                RequestStatus.SUCCESS -> onUpdateAppIdSucceeded()
                RequestStatus.FAILED -> onUpdateAppIdFailed(it.error)
                else ->{
                }
            }
        })
    }

    private fun connectViews(view: View){
        mTextInputLayout = view.findViewById(R.id.app_user_id_text_input)
        mSubmitButton = view.findViewById(R.id.app_user_id_sumbit_button)
        mBackToLoginButton = view.findViewById(R.id.back_to_login_btn)
    }

    private fun setupListeners(){
        mTextInputLayout.editText?.doOnTextChanged { text, _, _, _ ->
            text?.let{
                mUserInput = it.toString()
                mTextInputLayout.error = null
                mSubmitButton.isEnabled = text.length >= MIN_CHAR_COUNT
            }
        }
        mSubmitButton.setOnClickListener {
            mUserViewModel.updateAppUserId(mUserInput)
            mSubmitButton.isEnabled = false
            mTextInputLayout.error = null
        }
        mBackToLoginButton.setOnClickListener {
            mUserViewModel.logout()
            mNavigator.navigate(R.id.action_createAppUserIdFragment_to_loginFragment)
        }
    }

    private fun onUpdateAppIdSucceeded(){
        val activity = requireActivity()
        Toast.makeText(activity,activity.getString(R.string.success),Toast.LENGTH_SHORT).show()
    }

    private fun onUpdateAppIdFailed(error: String){
        // run on UI thread
        lifecycleScope.launch(Dispatchers.Main){
            mTextInputLayout.error = error
        }
    }

    companion object{
        private const val MIN_CHAR_COUNT = 4
    }
}