package com.hotukrainianboyz.nearly.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.hotukrainianboyz.nearly.ApplicationPrefs
import com.hotukrainianboyz.nearly.R
import com.hotukrainianboyz.nearly.entity.Friend
import com.hotukrainianboyz.nearly.entity.User
import com.hotukrainianboyz.nearly.utils.apiUtils.UserStatus
import com.hotukrainianboyz.nearly.utils.image.ImageUtils
import com.hotukrainianboyz.nearly.utils.json.JsonUtils
import com.hotukrainianboyz.nearly.utils.preferences.stringLiveData
import com.hotukrainianboyz.nearly.viewModel.FriendsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserProfileFragment : Fragment() {

    private val mFriendsViewModel: FriendsViewModel by activityViewModels()
    private lateinit var mBlockUserBtn: Button
    private lateinit var mDeleteFriendBtn: Button
    private lateinit var mUserAvatarImageView: ImageView
    private lateinit var mUserNameTextView: TextView
    private lateinit var mAppUserIdTextView: TextView
    private lateinit var mUserStatusTextView: TextView
    private lateinit var mUserBio: TextView
    private lateinit var mNavigator: NavController
    private lateinit var mAppUser: User
    private lateinit var mProfileFriend: Friend

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments == null) {
            Log.e(TAG, "Fragment created without args")
            mNavigator.navigateUp()
        }
        connectViews(view)
        setup()
    }

    private fun connectViews(view: View) {
        mNavigator = findNavController()
        mBlockUserBtn = view.findViewById(R.id.block_user_btn)
        mDeleteFriendBtn = view.findViewById(R.id.delete_friend_btn)
        mUserAvatarImageView = view.findViewById(R.id.profile_user_avatar)
        mUserNameTextView = view.findViewById(R.id.profile_username)
        mAppUserIdTextView = view.findViewById(R.id.profile_app_user_id)
        mUserStatusTextView = view.findViewById(R.id.profile_user_status)
        mUserBio = view.findViewById(R.id.profile_bio)
    }

    private fun setup() {
        mAppUser = JsonUtils.jsonToUser(
            ApplicationPrefs.encryptedPrefs.stringLiveData(
                ApplicationPrefs.USER_KEY,
                ""
            ).value!!
        )
        val friendJson = requireArguments().getString(FRIEND)
        mProfileFriend = JsonUtils.jsonToFriend(friendJson!!)
        mBlockUserBtn.setOnClickListener {
            val onFailed = { showToast(getString(R.string.failed_to_block)) }
            mFriendsViewModel.blockUser(mProfileFriend, onFailed) {
                lifecycleScope.launch(Dispatchers.Main) {
                    mNavigator.navigateUp()
                }
            }
        }
        mDeleteFriendBtn.setOnClickListener {
            val onFailed = { showToast(getString(R.string.failed_to_delete)) }
            mFriendsViewModel.deleteFriend(mProfileFriend, onFailed) {
                lifecycleScope.launch(Dispatchers.Main) {
                    mNavigator.navigateUp()
                }
            }
        }

        ImageUtils.loadCircularImage(mProfileFriend.imageUrl, mUserAvatarImageView)
        mAppUserIdTextView.text = mProfileFriend.appUserId
        mUserNameTextView.text = mProfileFriend.name
        mUserBio.text = mProfileFriend.userBio
        when (mProfileFriend.userStatus) {
            UserStatus.ONLINE -> {
                mUserStatusTextView.text = getString(R.string.online)
                mUserStatusTextView.setTextColor(requireActivity().getColor(R.color.colorAccent))
            }
            UserStatus.OFFLINE -> {
                mUserStatusTextView.text = getString(R.string.offline)
                mUserStatusTextView.setTextColor(Color.GRAY)
            }
            UserStatus.BUSY -> {
                mUserStatusTextView.text = getString(R.string.busy)
                mUserStatusTextView.setTextColor(Color.RED)
            }
        }
    }

    private fun showToast(text: String) {
        lifecycleScope.launch(Dispatchers.Main){
            Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
        }

    }

    companion object {
        const val FRIEND = "friend"
        private const val TAG = "UserProfileFragment"
    }
}