package com.ukrainianboyz.nearly.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.ukrainianboyz.nearly.ApplicationPrefs
import com.ukrainianboyz.nearly.R
import com.ukrainianboyz.nearly.model.SecureUser
import com.ukrainianboyz.nearly.utils.apiUtils.FriendRequestStatus
import com.ukrainianboyz.nearly.utils.image.ImageUtils
import com.ukrainianboyz.nearly.utils.json.JsonUtils
import com.ukrainianboyz.nearly.utils.observer.observeOnce
import com.ukrainianboyz.nearly.utils.preferences.stringLiveData
import com.ukrainianboyz.nearly.viewModel.FriendsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchUsersAdapter internal constructor(
    private val context: Context,
    private val friendsViewModel: FriendsViewModel
) : RecyclerView.Adapter<SearchUsersAdapter.SearchUsersViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var users = emptyList<SecureUser>() // Cached copy of users
    private var currentFriendsMap =
        mutableMapOf<String, FriendRequestStatus>() // Ids present in the DB
    private val appUserId: String

    init {
        val prefs = ApplicationPrefs.encryptedPrefs
        val userJson = prefs.stringLiveData(ApplicationPrefs.USER_KEY, "").value
        appUserId = JsonUtils.jsonToUser(userJson!!).id
        friendsViewModel.friends.observeOnce {
            currentFriendsMap.clear()
            for (friend in it) currentFriendsMap[friend.id] = friend.friendRequestStatus
        }
    }

    inner class SearchUsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val userAvatar: ImageView = itemView.findViewById(R.id.user_avatar)
        val sendRequestButton: ImageButton = itemView.findViewById(R.id.send_friend_request_btn)
        val existingUserStatus: TextView = itemView.findViewById(R.id.search_existing_user_status)
        val blockUserButton: ImageButton = itemView.findViewById(R.id.block_user_btn)
        val appUserIdTextView: TextView = itemView.findViewById(R.id.search_app_user_id)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchUsersViewHolder {
        val itemView = inflater.inflate(R.layout.search_users_list_item, parent, false)
        return SearchUsersViewHolder(itemView)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: SearchUsersViewHolder, position: Int) {
        val currentListItem = users[position]
        holder.userName.text = currentListItem.userName
        holder.appUserIdTextView.text = currentListItem.appUserId
        ImageUtils.loadCircularImage(currentListItem.imageUrl, holder.userAvatar)

        when {
            // if found yourself
            currentListItem.userId == appUserId -> {
                holder.sendRequestButton.visibility = View.INVISIBLE
                holder.existingUserStatus.visibility = View.INVISIBLE
                holder.blockUserButton.visibility = View.INVISIBLE
            }
            // if this user is already in the DB somewhere
            currentFriendsMap.containsKey(currentListItem.userId) -> {
                holder.sendRequestButton.visibility = View.INVISIBLE
                holder.blockUserButton.visibility = View.INVISIBLE
                holder.existingUserStatus.visibility = View.VISIBLE
                holder.existingUserStatus.text = when (currentFriendsMap[currentListItem.userId]) {
                    FriendRequestStatus.ACCEPTED -> context.getString(R.string.friend)
                    FriendRequestStatus.OUTGOING -> context.getString(R.string.outgoing)
                    FriendRequestStatus.INCOMING -> context.getString(R.string.incoming)
                    FriendRequestStatus.BLOCKED -> context.getString(R.string.blocked)
                    else -> ""
                }
                // if user is not
            }
            else -> {
                holder.sendRequestButton.visibility = View.VISIBLE
                holder.blockUserButton.visibility = View.VISIBLE
                holder.existingUserStatus.visibility = View.INVISIBLE
                holder.sendRequestButton.setOnClickListener {
                    // change the user and notify
                    currentFriendsMap[currentListItem.userId] = FriendRequestStatus.OUTGOING
                    notifyItemChanged(position)
                    // if request fails, remove from Map( will allow to add again) and show toast
                    friendsViewModel.sendFriendRequest(currentListItem) {
                        onFailed(position,context.getString(R.string.adding))
                    }
                }
                holder.blockUserButton.setOnClickListener {
                    currentFriendsMap[currentListItem.userId] = FriendRequestStatus.BLOCKED
                    notifyItemChanged(position)
                    friendsViewModel.blockUser(currentListItem){
                        onFailed(position, context.getString(R.string.blocking))
                    }
                }
            }
        }
    }

    fun setUsers(users: List<SecureUser>) {
        this.users = users
        notifyDataSetChanged()
    }

    fun appendUsers(users: List<SecureUser>) {
        val newUsersList = mutableListOf<SecureUser>()
        newUsersList.addAll(this.users)
        newUsersList.addAll(users)
        this.users = newUsersList
        notifyDataSetChanged()

    }

    private fun onFailed(position: Int, operation: String) {
        val current = users[position]
        friendsViewModel.viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, "$operation ${current.userName} ${context.getString(R.string.failed)}", Toast.LENGTH_SHORT).show()
            currentFriendsMap.remove(current.userId)
            notifyItemChanged(position)
        }
    }
}