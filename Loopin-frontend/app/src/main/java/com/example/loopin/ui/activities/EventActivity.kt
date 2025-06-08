package com.example.loopin.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loopin.R
import com.example.loopin.databinding.ActivityEventBinding
import com.example.loopin.models.Event
import com.example.loopin.ui.events.ActionStatus
import com.example.loopin.ui.events.EventDetailViewModel
import com.example.loopin.ui.events.ParticipantAdapter
import com.example.loopin.ui.events.UserEventStatus
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class EventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventBinding
    private lateinit var viewModel: EventDetailViewModel
    private var currentEventId: Int = -1
    private lateinit var participantAdapter: ParticipantAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MyApp)
        super.onCreate(savedInstanceState)
        binding = ActivityEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this).get(EventDetailViewModel::class.java)
        currentEventId = intent.getIntExtra("EVENT_ID", -1)

        if (currentEventId == -1) {
            showError("Etkinlik ID'si bulunamadı.")
        }

        setupRecyclerView()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        if (currentEventId != -1) {
            // GÜNCELLENDİ: Artık tek bir yerden veri yüklüyoruz.
            viewModel.loadEventData(currentEventId)
        }
    }

    private fun observeViewModel() {
        viewModel.event.observe(this) { event ->
            event?.let { updateUi(it) }
        }

        viewModel.eventDeletedStatus.observe(this) { isDeleted ->
            if (isDeleted) {
                Toast.makeText(this, "Etkinlik başarıyla silindi.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.participants.observe(this) { participants ->
            participantAdapter.submitList(participants)
        }

        // YENİ: Kullanıcı durumunu gözlemle
        viewModel.userStatus.observe(this) { status ->
            // Menünün yeniden çizilmesini tetikle
            invalidateOptionsMenu()

            // Yükleme durumu için ProgressBar'ı yönet
            binding.progressBar.visibility = if (status is UserEventStatus.Loading) View.VISIBLE else View.GONE

            if (status is UserEventStatus.Error) {
                showError(status.message)
            }
        }

        // YENİ: Aksiyon durumunu gözlemle (Katıl/Ayrıl/Silme Hatası vb.)
        viewModel.actionStatus.observe(this) { status ->
            val message = when(status) {
                is ActionStatus.Success -> status.message
                is ActionStatus.Failure -> status.message
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    // YENİ: Menüyü durum'a göre hazırlayan fonksiyon
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val status = viewModel.userStatus.value

        val joinItem = menu?.findItem(R.id.action_join_event)
        val leaveItem = menu?.findItem(R.id.action_leave_event)
        val editItem = menu?.findItem(R.id.action_edit_event)
        val deleteItem = menu?.findItem(R.id.action_delete_event)

        // Önce hepsini gizle
        joinItem?.isVisible = false
        leaveItem?.isVisible = false
        editItem?.isVisible = false
        deleteItem?.isVisible = false

        when (status) {
            is UserEventStatus.Creator -> {
                editItem?.isVisible = true
                deleteItem?.isVisible = true
            }
            is UserEventStatus.Participant -> {
                leaveItem?.isVisible = true
            }
            is UserEventStatus.NotParticipant -> {
                joinItem?.isVisible = true
            }
            else -> {
                // Loading veya Error durumunda hiçbir şey gösterme
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    private fun setupRecyclerView() {
        participantAdapter = ParticipantAdapter { participant ->
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("USER_ID", participant.id)
            }
            startActivity(intent)
        }

        binding.participantsRecyclerview.apply {
            adapter = participantAdapter
            layoutManager = LinearLayoutManager(this@EventActivity)
        }
    }


    private fun updateUi(event: Event) {
        supportActionBar?.title = event.eventName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.eventTitleTextview.text = event.eventName
        binding.eventDescriptionTextview.text = event.description?.ifEmpty { "Açıklama belirtilmemiş." }

        val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val apiDateFormatAlternative = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr", "TR"))
        val displayTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun parseDate(dateString: String?) = try {
            if (dateString.isNullOrEmpty()) null
            else if (dateString.contains("T")) apiDateFormat.parse(dateString)
            else apiDateFormatAlternative.parse(dateString)
        } catch (e: Exception) { null }

        val startDate = parseDate(event.startTime)
        val endDate = parseDate(event.endTime)

        binding.eventDateTextview.text = startDate?.let { displayDateFormat.format(it) } ?: "Tarih Belirtilmemiş"
        binding.eventTimeTextview.text = if (startDate != null && endDate != null) {
            "${displayTimeFormat.format(startDate)} - ${displayTimeFormat.format(endDate)}"
        } else {
            "Saat Belirtilmemiş"
        }
        binding.eventLocationTextview.text = event.eventLocation?.ifEmpty { null } ?: "Konum Belirtilmemiş"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_event_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // GÜNCELLENDİ: Yeni menü seçenekleri eklendi
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_join_event -> {
                viewModel.joinEvent(currentEventId)
                true
            }
            R.id.action_leave_event -> {
                viewModel.leaveEvent(currentEventId)
                true
            }
            R.id.action_edit_event -> {
                val intent = Intent(this, CreateEventActivity::class.java).apply {
                    putExtra("EDIT_EVENT_ID", currentEventId)
                }
                startActivity(intent)
                true
            }
            R.id.action_delete_event -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Etkinliği Sil")
            .setMessage("Bu etkinliği silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.")
            .setPositiveButton("Sil") { _, _ -> viewModel.deleteEvent(currentEventId) }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showError(message: String) {
        binding.eventTitleTextview.text = "Hata"
        binding.eventDescriptionTextview.text = message
        // Diğer alanları gizleyebilir veya boşaltabilirsiniz.
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}