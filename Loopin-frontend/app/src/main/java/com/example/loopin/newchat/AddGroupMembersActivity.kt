package com.example.loopin.newchat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loopin.PreferenceManager
import com.example.loopin.databinding.ActivityAddGroupMembersBinding
import com.example.loopin.models.Friend
import androidx.core.widget.addTextChangedListener
import com.example.loopin.newchat.AddGroupMembersViewModel


import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddGroupMembersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddGroupMembersBinding
    private val viewModel: AddGroupMembersViewModel by viewModels()
    private lateinit var adapter: AddGroupMembersAdapter // Bu zaten doğru

    private var groupId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGroupMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getIntExtra("groupId", -1)
        if (groupId == -1) {
            Toast.makeText(this, "Group ID not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.loadFriendsForGroup(groupId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Member" // English
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = AddGroupMembersAdapter { friend, isChecked ->
            if (isChecked) {
                viewModel.selectFriend(friend)
            } else {
                viewModel.deselectFriend(friend)
            }
        }
        binding.recyclerViewFriends.apply {
            layoutManager = LinearLayoutManager(this@AddGroupMembersActivity)
            adapter = this@AddGroupMembersActivity.adapter
        }
    }

    private fun setupListeners() {
        binding.etSearch.addTextChangedListener { editable ->
            viewModel.searchFriends(editable.toString())
        }

        binding.btnAddSelectedMembers.setOnClickListener {
            viewModel.addSelectedFriendsToGroup(groupId)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.friends.collectLatest { friends ->
                adapter.submitList(friends)
                binding.tvFriendsCount.text = "${friends.size} Friends" // English
            }
        }

        lifecycleScope.launch {
            viewModel.addGroupMemberStatus.collectLatest { status ->
                status?.let { (success, message) ->
                    Toast.makeText(this@AddGroupMembersActivity, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        viewModel.loadFriendsForGroup(groupId)
                        // finish() // Uncomment to close activity after successful addition
                    }
                    viewModel.resetAddGroupMemberStatus()
                }
            }
        }
    }
}