package com.ukrainianboyz.nearly.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ClipDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.ukrainianboyz.nearly.R
import com.ukrainianboyz.nearly.adapter.SearchUsersAdapter
import com.ukrainianboyz.nearly.repository.FriendsRepository
import com.ukrainianboyz.nearly.utils.apiUtils.RequestStatus
import com.ukrainianboyz.nearly.viewModel.FriendsViewModel
import kotlinx.coroutines.*


class SearchUsersFragment : Fragment() {

    private val friendsViewModel: FriendsViewModel by activityViewModels()
    private lateinit var searchField: SearchView
    private lateinit var loadMoreBtn: ExtendedFloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchUsersAdapter
    private lateinit var asyncLoadingJob: Job
    private lateinit var spinner: ProgressBar
    private lateinit var noResultsTextView: TextView
    private var isLoadingUsers: MutableLiveData<RequestStatus> =
        MutableLiveData(RequestStatus.UNDEFINED)
    private var isFragmentInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_users, container, false)
    }

    override fun onPause() {
        super.onPause()
        isLoadingUsers.value = RequestStatus.UNDEFINED
        searchField.setQuery("",false)
        view?.let { hideKeyboard(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectViews(view)
        setupAdapter()
        setupTextChangeListener()
        isFragmentInitialized = true
        isLoadingUsers.observe(viewLifecycleOwner, Observer {
            spinner.visibility = View.INVISIBLE
            when (it) {
                RequestStatus.PENDING -> spinner.visibility = View.VISIBLE
                RequestStatus.FAILED -> showFailedToast()
                else -> { }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if(isFragmentInitialized){
            setupAdapter()
        }
    }

    private fun connectViews(view: View) {
        searchField = view.findViewById(R.id.search_users_field)
        recyclerView = view.findViewById(R.id.search_users_list)
        spinner = view.findViewById(R.id.users_loading_progress_bar)
        noResultsTextView = view.findViewById(R.id.no_results_text_view)
        loadMoreBtn = view.findViewById(R.id.load_more_btn)
        loadMoreBtn.setOnClickListener{loadMoreUsers()}
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupAdapter() {
        val activity = requireActivity()
        adapter = SearchUsersAdapter(activity,friendsViewModel)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(false)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                ClipDrawable.HORIZONTAL
            )
        )
        recyclerView.setOnTouchListener { v, _ ->
            hideKeyboard(v)
            false
        }
    }

    private fun showFailedToast() {
        Toast.makeText(requireActivity(), "Loading failed!", Toast.LENGTH_LONG).show()
    }

    private fun setupTextChangeListener() {

        searchField.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(text: String?): Boolean {
                //first clear current list
                adapter.setUsers(emptyList())
                noResultsTextView.visibility = View.INVISIBLE
                loadMoreBtn.visibility = View.INVISIBLE
                // if something was loading, cancel it
                if (this@SearchUsersFragment::asyncLoadingJob.isInitialized && asyncLoadingJob.isActive) {
                    asyncLoadingJob.cancel()
                    isLoadingUsers.value = RequestStatus.UNDEFINED
                }
                // if no input, do nothing
                if (text.isNullOrBlank()) return true

                //start loading
                isLoadingUsers.value = RequestStatus.PENDING
                val networkRequestJob = friendsViewModel.getTopUsersAsync(text.toString(), true)

                asyncLoadingJob = lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val users = networkRequestJob.await() //load users
                        isLoadingUsers.postValue(RequestStatus.SUCCESS)
                        launch(Dispatchers.Main) {
                            if(users.isEmpty()) {
                                noResultsTextView.visibility = View.VISIBLE
                                loadMoreBtn.visibility = View.INVISIBLE
                            }else{
                                loadMoreBtn.visibility = View.VISIBLE
                            }
                            adapter.setUsers(users)  // update UI
                        }
                    } catch (e: CancellationException) {
                        networkRequestJob.cancelAndJoin() // if outer job was cancelled, must cancel this one as well
                    } catch (e: Exception) {
                        e.printStackTrace()
                        isLoadingUsers.postValue(RequestStatus.FAILED) // something actually failed
                    }
                }
                return true
            }

        })
    }

    private fun hideKeyboard(v: View){
        searchField.clearFocus()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    private fun loadMoreUsers() {
        loadMoreBtn.visibility = View.INVISIBLE
        isLoadingUsers.value = RequestStatus.PENDING
        val networkRequestJob = friendsViewModel.getTopUsersAsync(searchField.query.toString(), false)
        asyncLoadingJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                val users = networkRequestJob.await() //load users
                isLoadingUsers.postValue(RequestStatus.SUCCESS)
                launch(Dispatchers.Main) {
                    // if got less than requested can hide the button
                    loadMoreBtn.visibility = if(users.size < FriendsRepository.SEARCH_LOAD_AMOUNT) View.INVISIBLE else View.VISIBLE
                    adapter.appendUsers(users)  // update UI
                }
            } catch (e: CancellationException) {
                networkRequestJob.cancelAndJoin() // if outer job was cancelled, must cancel this one as well
            } catch (e: Exception) {
                e.printStackTrace()
                isLoadingUsers.postValue(RequestStatus.FAILED) // something actually failed
            }
        }

    }

    companion object{
        private const val TAG = "SearchUsersFragment"
    }


}