package com.example.loopin.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.loopin.R
import com.example.loopin.databinding.ActivityEventBinding
import com.example.loopin.models.Event
import com.example.loopin.ui.events.EventDetailViewModel

class EventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventBinding
    private lateinit var viewModel: EventDetailViewModel // YENİ: ViewModel referansı
    private var currentEventId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MyApp)
        super.onCreate(savedInstanceState)
        binding = ActivityEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Yeni ViewModel'i bağlıyoruz.
        viewModel = ViewModelProvider(this).get(EventDetailViewModel::class.java)

        currentEventId = intent.getIntExtra("EVENT_ID", -1)

        if (currentEventId != -1) {
            // DEĞİŞTİ: Artık ViewModel'den veri çekmesini istiyoruz.
            viewModel.fetchEventDetails(currentEventId)
        } else {
            showError("Etkinlik ID'si bulunamadı.")
        }

        // YENİ: ViewModel'deki değişiklikleri gözlemleyen fonksiyonu çağırıyoruz.
        observeViewModel()
    }
    override fun onResume() {
        super.onResume()
        // Bu ekran her açıldığında veya geri dönüldüğünde,
        // verinin en güncel halini sunucudan çek.
        if (currentEventId != -1) {
            viewModel.fetchEventDetails(currentEventId)
        } else {
            showError("Etkinlik ID'si bulunamadı.")
        }
    }

    // YENİ: ViewModel'i dinleyip arayüzü güncelleyen fonksiyon
    private fun observeViewModel() {
        // Etkinlik detayları geldiğinde...
        viewModel.event.observe(this) { event ->
            if (event != null) {
                updateUi(event)
            } else {
                showError("Etkinlik yüklenemedi veya bulunamadı.")
            }
        }

        // Silme işlemi sonucu geldiğinde...
        viewModel.eventDeletedStatus.observe(this) { isDeleted ->
            if (isDeleted) {
                Toast.makeText(this, "Etkinlik başarıyla silindi.", Toast.LENGTH_SHORT).show()
                finish() // Ekranı kapatıp listeye geri dön.
            } else {
                // Başarısızlık durumunda bir mesaj gösterebiliriz, şimdilik boş bırakıyoruz.
            }
        }
    }

    // YENİ: Arayüzü gelen etkinliğe göre dolduran fonksiyon
    private fun updateUi(event: Event) {
        supportActionBar?.title = event.eventName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.eventTitleTextview.text = event.eventName

        val details = StringBuilder()
        event.eventLocation?.let { if (it.isNotEmpty()) details.append("Konum: $it\n\n") }
        event.description?.let { if (it.isNotEmpty()) details.append(it) }

        // Tarih ve saatleri de ekleyelim (API'den doğru formatta geldiğini varsayarak)
        details.append("\nBaşlangıç: ${event.startTime}")
        details.append("\nBitiş: ${event.endTime}")

        binding.eventDetailsTextview.text = details.toString().ifEmpty { "Ek detay belirtilmemiş." }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_event_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Etkinliği Sil")
            .setMessage("Bu etkinliği silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.")
            .setPositiveButton("Sil") { _, _ ->
                // DEĞİŞTİ: Artık silme işlemini ViewModel'e devrediyoruz.
                viewModel.deleteEvent(currentEventId)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showError(message: String) {
        binding.eventTitleTextview.text = "Hata"
        binding.eventDetailsTextview.text = message
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}