package com.example.loopin.newchat

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loopin.PreferenceManager
import com.example.loopin.R
import com.example.loopin.databinding.ActivityGroupMembersBinding
import com.example.loopin.models.GroupMember
import com.example.loopin.models.RemoveGroupMemberRequest
import com.example.loopin.models.UpdateMemberRoleRequest
import com.example.loopin.network.ApiClient
import com.example.loopin.newchat.GroupMembersAdapter
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GroupMembersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupMembersBinding
    private val viewModel: GroupMembersViewModel by viewModels()
    private lateinit var adapter: GroupMembersAdapter

    private var groupId: Int = -1
    private var currentUserId: Int = -1
    private var currentUserRole: String = "member"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getIntExtra("groupId", -1)
        if (groupId == -1) {
            Toast.makeText(this, "Group ID not found.", Toast.LENGTH_SHORT).show() // English
            finish()
            return
        }
        currentUserId = PreferenceManager.getUserId() ?: -1

        setupToolbar()
        setupRecyclerView()
        setupFab() // BURAYI EKLEDİK: setupFab() metodunu çağırın.
        observeViewModel()

        viewModel.loadGroupMembers(groupId)
        viewModel.loadCurrentUserRole(groupId, currentUserId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Group Members" // English
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = GroupMembersAdapter { groupMember ->
            if (currentUserRole == "admin" || currentUserRole == "creator") {
                if (groupMember.userId == currentUserId) {
                    Toast.makeText(this, "You cannot perform actions on yourself.", Toast.LENGTH_SHORT).show() // English
                    return@GroupMembersAdapter
                }
                if (groupMember.role == "creator") {
                    Toast.makeText(this, "You cannot manage the group creator.", Toast.LENGTH_SHORT).show() // English
                    return@GroupMembersAdapter
                }
                showMemberActionsMenu(groupMember)
            } else {
                Toast.makeText(this, "Showing profile of ${groupMember.fullName}", Toast.LENGTH_SHORT).show() // English
            }
        }
        binding.recyclerViewMembers.apply {
            layoutManager = LinearLayoutManager(this@GroupMembersActivity)
            adapter = this@GroupMembersActivity.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddMember.setOnClickListener {
            val intent = Intent(this, AddGroupMembersActivity::class.java).apply {
                putExtra("groupId", groupId)
            }
            startActivity(intent)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.currentUserRole.collectLatest { role ->
                // FAB'ın görünürlüğünü güncelle
                if (role == "admin" || role == "creator") {
                    binding.fabAddMember.visibility = View.VISIBLE
                } else {
                    binding.fabAddMember.visibility = View.GONE
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.groupMembers.collectLatest { members ->
                adapter.submitList(members)
                binding.tvMemberCount.text = "${members.size} Members" // English
            }
        }

        lifecycleScope.launch {
            viewModel.currentUserRole.collectLatest { role ->
                currentUserRole = role
            }
        }

        lifecycleScope.launch {
            viewModel.actionStatus.collectLatest { status ->
                status?.let { (success, message) ->
                    Toast.makeText(this@GroupMembersActivity, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        viewModel.loadGroupMembers(groupId)
                    }
                    viewModel.resetActionStatus()
                }
            }
        }
    }

    private fun showMemberActionsMenu(member: GroupMember) {
        val popup = PopupMenu(this, binding.recyclerViewMembers.findViewHolderForAdapterPosition(adapter.currentList.indexOf(member))?.itemView)
        popup.menuInflater.inflate(R.menu.group_member_actions_menu, popup.menu)

        if (member.role == "admin") {
            popup.menu.findItem(R.id.action_make_admin)?.isVisible = false
            popup.menu.findItem(R.id.action_make_member)?.isVisible = true
        } else {
            popup.menu.findItem(R.id.action_make_admin)?.isVisible = true
            popup.menu.findItem(R.id.action_make_member)?.isVisible = false
        }

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.action_make_admin -> {
                    viewModel.updateMemberRole(groupId, member.userId, "admin")
                    true
                }
                R.id.action_make_member -> {
                    viewModel.updateMemberRole(groupId, member.userId, "member")
                    true
                }
                R.id.action_remove_member -> {
                    viewModel.removeGroupMember(groupId, member.userId)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}