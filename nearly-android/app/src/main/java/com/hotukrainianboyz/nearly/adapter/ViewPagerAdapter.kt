package com.hotukrainianboyz.nearly.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hotukrainianboyz.nearly.R
import com.hotukrainianboyz.nearly.fragment.SearchUsersFragment
import com.hotukrainianboyz.nearly.fragment.IncomingFriendRequestsFragment
import com.hotukrainianboyz.nearly.fragment.OutgoingFriendRequestsFragment

class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment)  {

    private val fragments = listOf(SearchUsersFragment(),IncomingFriendRequestsFragment(),OutgoingFriendRequestsFragment())
    val viewPagerTitles = arrayListOf(fragment.getString(R.string.search),fragment.getString(R.string.incoming),fragment.getString(R.string.outgoing))

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}