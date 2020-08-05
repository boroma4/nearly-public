package com.hotukrainianboyz.nearly.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.hotukrainianboyz.nearly.R
import com.hotukrainianboyz.nearly.entity.Friend
import com.hotukrainianboyz.nearly.utils.image.ImageUtils
import com.hotukrainianboyz.nearly.viewModel.FriendsViewModel

class BlockedUsersAdapter internal constructor(private val context: Context, private val viewModel: FriendsViewModel)  :
    RecyclerView.Adapter<BlockedUsersAdapter.BlockedUsersViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var users = emptyList<Friend>() // Cached copy of users

    inner class BlockedUsersViewHolder(itemView: View)  : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val userAvatar: ImageView = itemView.findViewById(R.id.user_avatar)
        val unblockUserButton: Button = itemView.findViewById(R.id.unblock_user_btn)
        val appUserIdTextView: TextView = itemView.findViewById(R.id.blocked_user_app_user_id)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BlockedUsersViewHolder {
        val itemView = inflater.inflate(R.layout.blocked_users_list_item, parent, false)
        return BlockedUsersViewHolder(itemView)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: BlockedUsersViewHolder, position: Int) {
        val current = users[position]
        holder.userName.text = current.name
        holder.appUserIdTextView.text = current.appUserId
        ImageUtils.loadCircularImage(current.imageUrl, holder.userAvatar)
        holder.unblockUserButton.setOnClickListener {
            viewModel.unblockUser(current){
                // on failed
                Toast.makeText(context, context.getString(R.string.unblocking_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setUsers(users: List<Friend>){
        this.users = users
        notifyDataSetChanged()
    }
}