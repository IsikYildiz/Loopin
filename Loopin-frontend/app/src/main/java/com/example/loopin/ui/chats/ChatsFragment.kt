package com.example.loopin.ui.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loopin.databinding.FragmentChatsBinding
import com.example.loopin.ui.activities.EXTRA_GROUP_ID
import com.example.loopin.ui.activities.EXTRA_GROUP_NAME
import com.example.loopin.ui.activities.GroupChatActivity
import com.google.android.material.snackbar.Snackbar

class ChatsFragment : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatsViewModel: ChatsViewModel
    private lateinit var groupAdapter: GroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        chatsViewModel = ViewModelProvider(this).get(ChatsViewModel::class.java)
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()

        // Veri yükleme işlemini SADECE view oluşturulduğunda bir kereliğine tetikle.
        // ViewModel veriyi zaten hafızasında tutacaktır.
        chatsViewModel.fetchUserGroups()

    }

    private fun setupRecyclerView() {
        groupAdapter = GroupAdapter { group ->
            val intent = android.content.Intent(requireActivity(), GroupChatActivity::class.java).apply {
                putExtra(EXTRA_GROUP_ID, group.groupId)
                putExtra(EXTRA_GROUP_NAME, group.groupName)
            }
            startActivity(intent)
        }
        binding.recyclerViewGroups.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        chatsViewModel.groups.observe(viewLifecycleOwner) { groups ->
            groupAdapter.submitList(groups)
            binding.textViewNoGroups.isVisible = groups.isEmpty()
        }

        chatsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarGroups.isVisible = isLoading
        }

        chatsViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Binding referansını temizle
        binding.recyclerViewGroups.adapter = null
        _binding = null
    }
}