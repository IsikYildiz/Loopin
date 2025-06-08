package com.example.loopin.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loopin.databinding.FragmentHomeBinding
import com.example.loopin.ui.activities.EventActivity
import com.example.loopin.ui.events.EventAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Sayfa her açıldığında verileri yeniden çek
        homeViewModel.fetchPublicEvents() // <-- Burayı da fetchAllUpcomingEvents'ten fetchPublicEvents'e güncelleyin.
    }

    private fun setupRecyclerView() {
        // EventAdapter, bir etkinliğe tıklandığında EventActivity'yi açar
        eventAdapter = EventAdapter { event ->
            val intent = Intent(requireActivity(), EventActivity::class.java).apply {
                putExtra("EVENT_ID", event.eventId)
            }
            startActivity(intent)
        }

        binding.recyclerViewEvents.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        // Etkinlik listesini gözle ve adapter'a gönder
        homeViewModel.events.observe(viewLifecycleOwner) { events ->
            eventAdapter.submitList(events)
            binding.textViewError.isVisible = events.isNullOrEmpty() && !homeViewModel.isLoading.value!!
            if (binding.textViewError.isVisible) {
                binding.textViewError.text = "Gösterilecek etkinlik bulunamadı."
            }
        }

        // Yükleme durumunu gözle ve ProgressBar'ı göster/gizle
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        // Hata durumunu gözle ve hata mesajını göster/gizle
        homeViewModel.error.observe(viewLifecycleOwner) { error ->
            binding.textViewError.isVisible = error != null
            binding.textViewError.text = error
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}