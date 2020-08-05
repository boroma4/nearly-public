package com.hotukrainianboyz.nearly.adapter

import android.app.ActivityOptions
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.hotukrainianboyz.nearly.R
import com.hotukrainianboyz.nearly.activity.VideoCallActivity
import com.hotukrainianboyz.nearly.entity.Friend
import com.hotukrainianboyz.nearly.fragment.UserProfileFragment
import com.hotukrainianboyz.nearly.utils.apiUtils.UserStatus
import com.hotukrainianboyz.nearly.utils.image.ImageUtils
import com.hotukrainianboyz.nearly.utils.json.JsonUtils


class FriendsAdapter internal constructor(
    private val context: Context,
    private val navigator: NavController
) :
    RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var users = emptyList<Friend>() // Cached copy of users
    private var online = false

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val userAvatar: ImageView = itemView.findViewById(R.id.user_avatar)
        val cameraButton: ImageButton = itemView.findViewById(R.id.video_call_icon)
        val friendStatus: TextView = itemView.findViewById(R.id.user_status)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendsAdapter.FriendViewHolder {
        val itemView = inflater.inflate(R.layout.friends_list_item, parent, false)
        return FriendViewHolder(itemView)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(
        holder: FriendsAdapter.FriendViewHolder,
        position: Int
    ) {
        val current = users[position]
        holder.userName.text = current.name
        ImageUtils.loadCircularImage(current.imageUrl, holder.userAvatar)
        val isAvailable = online && current.userStatus != UserStatus.BUSY
        when (current.userStatus) {
            UserStatus.ONLINE -> {
                holder.friendStatus.text = context.getString(R.string.online)
                holder.friendStatus.setTextColor(context.getColor(R.color.colorAccent))
            }
            UserStatus.OFFLINE -> {
                holder.friendStatus.text = context.getString(R.string.offline)
                holder.friendStatus.setTextColor(Color.GRAY)
            }
            UserStatus.BUSY -> {
                holder.friendStatus.text = context.getString(R.string.busy)
                holder.friendStatus.setTextColor(Color.RED)
            }
        }
        holder.cameraButton.isEnabled = isAvailable
        holder.cameraButton.setImageResource(if (isAvailable) R.drawable.ic_video_call_black_24dp else R.drawable.ic_error_black_24dp)
        holder.cameraButton.setOnClickListener {
            val options = ActivityOptions.makeCustomAnimation(
                context,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            context.startActivity(
                //GameActivity.makeIntent(context),
                VideoCallActivity.makeIntent(context, current.id, true),
                options.toBundle()
            )
        }
        holder.userAvatar.setOnClickListener {
            navigator.navigate(
                R.id.action_friendsFragment_to_userProfileFragment,
                bundleOf(UserProfileFragment.FRIEND to JsonUtils.toJson(current))
            )
        }
    }

    internal fun setOnline(online: Boolean) {
        this.online = online
        notifyDataSetChanged()
    }

    fun setUsers(users: List<Friend>) {
        this.users = users
        notifyDataSetChanged()
    }
}