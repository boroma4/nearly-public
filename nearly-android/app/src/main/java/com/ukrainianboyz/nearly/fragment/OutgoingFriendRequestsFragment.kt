package com.ukrainianboyz.nearly.fragment

import android.graphics.drawable.ClipDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ukrainianboyz.nearly.R
import com.ukrainianboyz.nearly.adapter.OutgoingFriendRequestsAdapter
import com.ukrainianboyz.nearly.utils.apiUtils.FriendRequestStatus
import com.ukrainianboyz.nearly.viewModel.FriendsViewModel

class OutgoingFriendRequestsFragment : Fragment() {

    private val friendsViewModel: FriendsViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OutgoingFriendRequestsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_outgoing_friend_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectViews(view)
        setupAdapter()
        friendsViewModel.friends.observe(viewLifecycleOwner, Observer { users ->
            // Update the cached copy of the words in the adapter.
            users?.let { entries -> adapter.setUsers(entries.filter { it.friendRequestStatus == FriendRequestStatus.OUTGOING }) }
        })
    }

    private fun connectViews(view: View){
        recyclerView = view.findViewById(R.id.outgoing_friend_requests_list)
    }

    private fun setupAdapter(){
        val activity = requireActivity()
        adapter = OutgoingFriendRequestsAdapter(activity, friendsViewModel)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), ClipDrawable.HORIZONTAL))
    }

}