package com.example.loopin.ui.events

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loopin.databinding.FragmentEventsBinding
import com.example.loopin.ui.activities.CreateEventActivity
import com.example.loopin.ui.activities.EventActivity

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventsViewModel: EventsViewModel
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        eventsViewModel = ViewModelProvider(this).get(EventsViewModel::class.java)
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        // LiveData'yı gözlemlemeye devam ediyoruz. Veri geldiğinde liste güncellenecek.
        eventsViewModel.events.observe(viewLifecycleOwner) { events ->
            eventAdapter.submitList(events)
        }

        binding.addEventFab.setOnClickListener {
            val intent = Intent(requireActivity(), CreateEventActivity::class.java)
            startActivity(intent)
        }
    }

    // YENİ EKLENEN FONKSİYON
    override fun onResume() {
        super.onResume()
        // Bu fragment her ekrana geldiğinde (örneğin etkinlik oluşturup geri dönüldüğünde)
        // verilerin en güncel halini çekmesi için ViewModel'i tetikliyoruz.
        eventsViewModel.fetchMyCreatedEvents()
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter { event ->
            val intent = Intent(activity, EventActivity::class.java).apply {
                putExtra("EVENT_ID", event.eventId)
            }
            startActivity(intent)
        }

        binding.eventsRecyclerView.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}