package com.example.loopin.ui.chats

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.loopin.PreferenceManager // Assuming PreferenceManager exists
import com.example.loopin.R
import com.example.loopin.databinding.FragmentChatsBinding
import com.example.loopin.ui.chats.adapter.AllChatsAdapter // Import new adapter
import com.example.loopin.ui.chats.adapter.ChatAdapter
import com.example.loopin.ui.chats.adapter.GroupAdapter
import com.example.loopin.newchat.CreateNewChatActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatsFragment : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        // Fragment tekrar görünür olduğunda grup listesini yenile
        viewModel.refreshChats()
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = ChatsPagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Tümü"
                1 -> "Birebir"
                else -> "Gruplar"
            }
        }.attach()
    }

    private fun setupFab() {
        binding.fabNewChat.setOnClickListener {
            // Yeni oluşturduğumuz CreateNewChatActivity'yi başlat
            val intent = Intent(requireContext(), CreateNewChatActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class ChatsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AllChatsFragment()
                1 -> IndividualChatsFragment()
                else -> GroupChatsFragment()
            }
        }
    }
}

@AndroidEntryPoint
class AllChatsFragment : Fragment() {
    private val viewModel: ChatsViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: AllChatsAdapter // Use AllChatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = PreferenceManager.getUserId() ?: -1 // Get current user ID
        adapter = AllChatsAdapter(onItemClick = { chatListItem ->
            val intent = Intent(requireContext(), ChatMessageActivity::class.java).apply {
                when (chatListItem) {
                    is AllChatInfoItem -> putExtra("chatId", chatListItem.chat.chatId) // Access actual ChatInfo from wrapper
                    is AllGroupInfoItem -> putExtra("groupId", chatListItem.group.groupId) // Access actual GroupInfo from wrapper
                }
            }
            startActivity(intent)
        }, currentUserId = currentUserId)

        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allChats.collectLatest { chats ->
                adapter.submitList(chats)
            }
        }
    }
}

@AndroidEntryPoint
class IndividualChatsFragment : Fragment() {
    private val viewModel: ChatsViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatAdapter { chat ->
            val intent = Intent(requireContext(), ChatMessageActivity::class.java).apply {
                putExtra("chatId", chat.chatId)
            }
            startActivity(intent)
        }

        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.individualChats.collectLatest { chats ->
                adapter.submitList(chats)
            }
        }
    }
}

@AndroidEntryPoint
class GroupChatsFragment : Fragment() {
    private val viewModel: ChatsViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: GroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = GroupAdapter { group ->
            val intent = Intent(requireContext(), ChatMessageActivity::class.java).apply {
                putExtra("groupId", group.groupId)
            }
            startActivity(intent)
        }

        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.groupChats.collectLatest { groups ->
                adapter.submitList(groups)
            }
        }
    }
}