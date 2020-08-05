package com.hotukrainianboyz.nearly.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.hotukrainianboyz.nearly.R
import com.hotukrainianboyz.nearly.viewModel.UserViewModel

/**
 * A simple [Fragment] subclass.
 */
class LogoutFragment : Fragment() {

    private val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_logout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navigator = findNavController()
        viewModel.logout()
        navigator.navigate(R.id.action_logoutFragment_to_loginActivity)
        requireActivity().finish()
    }

}
