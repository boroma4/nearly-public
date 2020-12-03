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
import com.ukrainianboyz.nearly.R
import com.ukrainianboyz.nearly.entity.Friend
import com.ukrainianboyz.nearly.utils.image.ImageUtils
import com.ukrainianboyz.nearly.viewModel.FriendsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class IncomingFriendRequestsAdapter internal constructor(private val context: Context,private val viewModel: FriendsViewModel) :
    RecyclerView.Adapter<IncomingFriendRequestsAdapter.IncomingFriendRequestViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var users = emptyList<Friend>() // Cached copy of users

    inner class IncomingFriendRequestViewHolder(itemView: View)  : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val userAvatar: ImageView = itemView.findViewById(R.id.user_avatar)
        val acceptRequestButton: ImageButton = itemView.findViewById(R.id.accept_incoming_friend_request)
        val declineRequestButton: ImageButton = itemView.findViewById(R.id.decline_incoming_friend_request)
        val blockUserButton: ImageButton = itemView.findViewById(R.id.block_user_btn)
        val appUserIdTextView: TextView = itemView.findViewById(R.id.in_req_app_user_id)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IncomingFriendRequestViewHolder {
        val itemView = inflater.inflate(R.layout.incoming_friend_request_list_item, parent, false)
        return IncomingFriendRequestViewHolder(itemView)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: IncomingFriendRequestViewHolder, position: Int) {
        val current = users[position]
        holder.userName.text = current.name
        holder.appUserIdTextView.text = current.appUserId
        ImageUtils.loadCircularImage(current.imageUrl, holder.userAvatar)
        holder.acceptRequestButton.setOnClickListener {
            viewModel.respondToFriendRequest(current.id,true) {onRespondFailed()}
        }
        holder.declineRequestButton.setOnClickListener {
            viewModel.respondToFriendRequest(current.id,false) {onRespondFailed()}
        }
        holder.blockUserButton.setOnClickListener {
            viewModel.blockUser(current, onFailed = { onBlockFailed() })
        }
    }

    fun setUsers(users: List<Friend>){
        this.users = users
        notifyDataSetChanged()
    }

    private fun onRespondFailed() = showToast(context.getString(R.string.responding_to_request_failed))

    private fun onBlockFailed() = showToast(context.getString(R.string.failed_to_block))

    private fun showToast(text: String){
        viewModel.viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, text , Toast.LENGTH_SHORT).show()
        }
    }
}