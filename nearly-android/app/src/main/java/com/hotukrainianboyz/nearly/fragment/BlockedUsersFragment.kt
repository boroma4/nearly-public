package com.hotukrainianboyz.nearly.fragment

import android.graphics.drawable.ClipDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hotukrainianboyz.nearly.R
import com.hotukrainianboyz.nearly.adapter.BlockedUsersAdapter
import com.hotukrainianboyz.nearly.adapter.OutgoingFriendRequestsAdapter
import com.hotukrainianboyz.nearly.utils.apiUtils.FriendRequestStatus
import com.hotukrainianboyz.nearly.viewModel.FriendsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BlockedUsersFragment : Fragment() {

    private val friendsViewModel: FriendsViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BlockedUsersAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blocked_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectViews(view)
        setupAdapter()
        setupSwipeToRefresh()
        friendsViewModel.friends.observe(viewLifecycleOwner, Observer { users ->
            // Update the cached copy of the words in the adapter.
            users?.let { entries -> adapter.setUsers(entries.filter { it.friendRequestStatus == FriendRequestStatus.BLOCKED }) }
        })
    }

    private fun connectViews(view: View){
        recyclerView = view.findViewById(R.id.blocked_users_list)
        swipeRefreshLayout = view.findViewById(R.id.blocked_users_refresh_layout)
    }

    private fun setupAdapter(){
        val activity = requireActivity()
        adapter = BlockedUsersAdapter(activity, friendsViewModel)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), ClipDrawable.HORIZONTAL))
    }

    private fun setupSwipeToRefresh(){
        swipeRefreshLayout.setOnRefreshListener {
            // This method performs the actual data-refresh operation.
            // The method calls setRefreshing(false) when it's finished.
            friendsViewModel.fetchBlockedUsers({swipeRefreshLayout.isRefreshing = false}) {
                lifecycleScope.launch(Dispatchers.Main) {
                    swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(requireActivity(),R.string.failed_to_load_blocked_users, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}