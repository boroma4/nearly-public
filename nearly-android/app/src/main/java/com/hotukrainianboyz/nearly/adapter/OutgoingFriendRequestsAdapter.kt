package com.hotukrainianboyz.nearly.adapter

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
import com.hotukrainianboyz.nearly.R
import com.hotukrainianboyz.nearly.entity.Friend
import com.hotukrainianboyz.nearly.utils.image.ImageUtils
import com.hotukrainianboyz.nearly.viewModel.FriendsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OutgoingFriendRequestsAdapter  internal constructor(private val context: Context, private val viewModel: FriendsViewModel)  :
    RecyclerView.Adapter<OutgoingFriendRequestsAdapter.OutgoingFriendRequestsViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var users = emptyList<Friend>() // Cached copy of users

    inner class OutgoingFriendRequestsViewHolder(itemView: View)  : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val userAvatar: ImageView = itemView.findViewById(R.id.user_avatar)
        val cancelRequestBtn: ImageButton = itemView.findViewById(R.id.cancel_outgoing_friend_request)
        val appUserIdTextView: TextView = itemView.findViewById(R.id.out_req_app_user_id)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OutgoingFriendRequestsViewHolder {
        val itemView = inflater.inflate(R.layout.outgoing_friend_request_list_item, parent, false)
        return OutgoingFriendRequestsViewHolder(itemView)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: OutgoingFriendRequestsViewHolder, position: Int) {
        val current = users[position]
        holder.userName.text = current.name
        holder.appUserIdTextView.text = current.appUserId
        ImageUtils.loadCircularImage(current.imageUrl, holder.userAvatar)
        holder.cancelRequestBtn.setOnClickListener {
            viewModel.cancelOutgoingFriendRequest(current){
                // on failed
                Toast.makeText(context, context.getString(R.string.removing_request_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setUsers(users: List<Friend>){
        this.users = users
        notifyDataSetChanged()
    }
}