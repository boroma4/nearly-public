package com.hotukrainianboyz.nearly.fragment

import android.app.Activity
import android.graphics.drawable.ClipDrawable.HORIZONTAL
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.hotukrainianboyz.nearly.R
import com.hotukrainianboyz.nearly.service.signalR.SignalRClient
import com.hotukrainianboyz.nearly.adapter.FriendsAdapter
import com.hotukrainianboyz.nearly.utils.apiUtils.FriendRequestStatus
import com.hotukrainianboyz.nearly.viewModel.FriendsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FriendsFragment : Fragment() {

    private val friendsViewModel: FriendsViewModel by activityViewModels()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: Toolbar
    private lateinit var adapter: FriendsAdapter
    private lateinit var addFriendButton: ExtendedFloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onResume() {
        super.onResume()
        if(this::toolbar.isInitialized && SignalRClient.isConnecting){
            toolbar.setTitle(R.string.connecting)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        connectViews(view,activity)
        setupAdapter(activity)
        setupSwipeToRefresh()
        friendsViewModel.friends.observe(viewLifecycleOwner, Observer { friends ->
            // Update the cached copy of the words in the adapter.
            friends?.let { entries -> adapter.setUsers(entries.filter { it.friendRequestStatus == FriendRequestStatus.ACCEPTED }) }
        })

        SignalRClient.isOnline.observe(viewLifecycleOwner, Observer { online ->
            adapter.setOnline(online)
            var title = R.string.app_name //normal case if online
            if(!online){
               title = if(SignalRClient.isConnecting) R.string.connecting else R.string.logging_out
            }
            toolbar.setTitle(title)
        })
    }

    private fun connectViews(view: View, activity: Activity){
        swipeRefreshLayout = view.findViewById(R.id.friends_list_refresh_layout)
        recyclerView = view.findViewById(R.id.friend_list)
        toolbar = activity.findViewById(R.id.main_toolbar)
        addFriendButton = view.findViewById(R.id.send_friend_request_btn)
        addFriendButton.setOnClickListener {
            findNavController().navigate(R.id.action_friendsFragment_to_friendRequestsFragment)
        }
    }

    private fun setupSwipeToRefresh(){
        swipeRefreshLayout.setOnRefreshListener {
            // This method performs the actual data-refresh operation.
            // The method calls setRefreshing(false) when it's finished.
            friendsViewModel.fetchFriendsAndRequests({swipeRefreshLayout.isRefreshing = false}) {
                lifecycleScope.launch(Dispatchers.Main) {
                    swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(requireActivity(),R.string.failed_to_load_friends, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupAdapter(activity: Activity){
        adapter = FriendsAdapter(activity, findNavController())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), HORIZONTAL))
    }

}
