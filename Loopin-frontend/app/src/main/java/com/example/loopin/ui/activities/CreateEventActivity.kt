package com.example.loopin.ui.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.loopin.PreferenceManager
import com.example.loopin.R
import com.example.loopin.databinding.ActivityCreateEventBinding
import com.example.loopin.models.Event
import com.example.loopin.ui.events.CreateEventViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.apply
import kotlin.jvm.java
import kotlin.let
import kotlin.text.contains
import kotlin.text.ifEmpty
import kotlin.text.isEmpty
import kotlin.text.toInt
import kotlin.text.toIntOrNull
import kotlin.text.trim
import kotlin.toString

class CreateEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEventBinding
    private lateinit var createEventViewModel: CreateEventViewModel

    // Başlangıç ve bitiş takvim nesneleri
    private val startCalendar: Calendar = Calendar.getInstance()
    private val endCalendar: Calendar = Calendar.getInstance()

    // Kullanıcının tarih ve saat seçip seçmediğini takip eden bayraklar
    private var isStartDateSet = false
    private var isStartTimeSet = false
    private var isEndDateSet = false
    private var isEndTimeSet = false

    // Bu değişken, aktivitenin "düzenleme" modunda mı (-1 değilse) yoksa "oluşturma" modunda mı (-1 ise)
    // çalıştığını ve hangi etkinliğin düzenlendiğini tutar.
    private var editModeEventId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MyApp)
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)

        setContentView(binding.root)



        createEventViewModel = ViewModelProvider(this).get(CreateEventViewModel::class.java)

        // Bir önceki ekrandan (EventActivity) gönderilen event ID'sini kontrol et.
        editModeEventId = intent.getIntExtra("EDIT_EVENT_ID", -1)

        // Eğer bir ID geldiyse düzenleme modunu, gelmediyse oluşturma modunu ayarla.
        if (editModeEventId != -1) {
            setupEditMode()
        } else {
            setupCreateMode()
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Geri tuşunu her iki modda da göster

        setupListeners()
        observeViewModel()
    }

    // Toolbar'daki geri okuna basıldığında aktiviteyi bitirir.
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * Aktivite "Yeni Etkinlik Oluştur" modundayken arayüzü ayarlar.
     */
    private fun setupCreateMode() {
        supportActionBar?.title = "Yeni Etkinlik Oluştur"
        binding.createEventButton.text = "Etkinliği Oluştur"
    }

    /**
     * Aktivite "Etkinliği Düzenle" modundayken arayüzü ayarlar.
     */
    private fun setupEditMode() {
        supportActionBar?.title = "Etkinliği Düzenle"
        binding.createEventButton.text = "Değişiklikleri Kaydet"
        // ViewModel'e, düzenlenecek etkinliğin verilerini sunucudan çekmesini söyle.
        createEventViewModel.loadEvent(editModeEventId)
    }

    /**
     * ViewModel'deki LiveData'ları gözlemleyerek arayüzü günceller.
     */
    private fun observeViewModel() {
        // Etkinlik oluşturma/güncelleme işleminin sonucunu dinler.

        createEventViewModel.operationStatus.observe(this) { success ->
            if (success) {
                // Mod'a göre doğru mesajı göster.
                val message = if (editModeEventId != -1) "Etkinlik başarıyla güncellendi!" else "Etkinlik başarıyla oluşturuldu!"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                finish() // İşlem başarılıysa ekranı kapat.
            } else {
                // Hata burada gösteriliyor!
                Toast.makeText(this, "İşlem sırasında bir hata oluştu.", Toast.LENGTH_SHORT).show()
            }
        }

        // Düzenlenecek etkinlik verisi ViewModel tarafından yüklendiğinde bu kod çalışır.
        createEventViewModel.eventToEdit.observe(this) { event ->
            // Gelen etkinlik verisi null değilse, form alanlarını bu veriyle doldur.
            event?.let { populateForm(it) }
        }
    }

    /**
     * Düzenleme modunda, formu sunucudan gelen etkinlik verileriyle doldurur.
     */
    // In CreateEventActivity.kt

    private fun populateForm(event: Event) {
        binding.eventNameEdittext.setText(event.eventName)
        binding.eventLocationEdittext.setText(event.eventLocation)
        binding.eventDescriptionEdittext.setText(event.description)
        binding.eventMaxParticipantsEdittext.setText(event.maxParticipants.toString())
        binding.privateEventSwitch.isChecked = event.isPrivate == 1

        if (binding.privateEventSwitch.isChecked) {
            binding.eventPasswordLayout.visibility = View.VISIBLE
            binding.eventPasswordEdittext.setText(event.password)
        }

        // --- DEĞİŞEN KISIM BURASI ---

        // 1. API'den gelen ISO 8601 formatını okumak için bir format tanımlıyoruz.
        // Sunucunuzun tam olarak hangi formatı gönderdiğine göre bu değişebilir,
        // ama genellikle bu ikisinden biridir.
        val apiDateFormat = if (event.startTime.contains("T")) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
        } else {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        }

        // 2. Arayüzde göstereceğimiz formatları tanımlıyoruz.
        val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val displayTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        try {
            // Başlangıç zamanını `apiDateFormat` ile OKU
            event.startTime.let {
                val date = apiDateFormat.parse(it)
                date?.let {
                    startCalendar.time = date
                    binding.startDateButton.text = displayDateFormat.format(date)
                    binding.startTimeButton.text = displayTimeFormat.format(date)
                    isStartDateSet = true
                    isStartTimeSet = true
                }
            }
            // Bitiş zamanını `apiDateFormat` ile OKU
            event.endTime.let {
                val date = apiDateFormat.parse(it)
                date?.let {
                    endCalendar.time = date
                    binding.endDateButton.text = displayDateFormat.format(date)
                    binding.endTimeButton.text = displayTimeFormat.format(date)
                    isEndDateSet = true
                    isEndTimeSet = true
                }
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            Toast.makeText(this, "Tarih formatı sunucudan okunamadı.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Ekrandaki butonların ve diğer bileşenlerin tıklama olaylarını ayarlar.
     */
    private fun setupListeners() {
        binding.startDateButton.setOnClickListener { showDatePicker(isStart = true) }
        binding.startTimeButton.setOnClickListener { showTimePicker(isStart = true) }
        binding.endDateButton.setOnClickListener { showDatePicker(isStart = false) }
        binding.endTimeButton.setOnClickListener { showTimePicker(isStart = false) }

        binding.privateEventSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.eventPasswordLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.createEventButton.setOnClickListener {
            validateAndProceed()
        }
    }

    /**
     * Formdaki verileri doğrular ve geçerliyse ViewModel'deki ilgili fonksiyonu çağırır.
     */
    private fun validateAndProceed() {
        // 1. Önceki Hataları Temizle
        // Fonksiyon her çalıştığında, bir önceki denemeden kalan hata mesajlarını temizleriz.
        binding.eventNameLayout.error = null
        binding.eventMaxParticipantsLayout.error = null

        // Formun genel geçerlilik durumunu tutan bir bayrak.
        var isValid = true

        // 2. Doğrulama Kontrolleri (İstediğiniz Sırada)

        // --- Kontrol 1: Etkinlik Adı ---
        val eventName = binding.eventNameEdittext.text.toString().trim()
        if (eventName.isEmpty()) {
            binding.eventNameLayout.error = "Etkinlik adı zorunludur"
            isValid = false
        }

        // --- Kontrol 2: Tarih ve Saat Seçimi ---
        if (!isStartDateSet || !isStartTimeSet) {
            // Bu alanların zorunlu olduğunu her durumda belirtiyoruz.
            Toast.makeText(this, "Lütfen başlangıç tarih ve saatini seçin.", Toast.LENGTH_LONG).show()
            isValid = false
        }

        // --- Kontrol 3: Maksimum Katılımcı Sayısı ---
        val maxParticipantsText = binding.eventMaxParticipantsEdittext.text.toString()
        if (maxParticipantsText.isEmpty()) {
            binding.eventMaxParticipantsLayout.error = "Katılımcı sayısı zorunludur"
            isValid = false
        } else if (maxParticipantsText.toIntOrNull() == 0) {
            binding.eventMaxParticipantsLayout.error = "Katılımcı sayısı 0 olamaz"
            isValid = false
        }

        // Eğer bu temel kontrollerden herhangi biri başarısız olduysa, işlemi burada durdur.
        if (!isValid) {
            return
        }

        // 3. Varsayılan Değerler ve Mantıksal Kontroller
        // (Temel kontrollerden geçildikten sonra çalışır)

        // Bitiş tarihi/saati girilmemişse, varsayılan değerleri ata.
        if (!isEndDateSet) {
            endCalendar.time = startCalendar.time
            updateDateButtonText(isStart = false)
        }
        if (!isEndTimeSet) {
            endCalendar.time = startCalendar.time
            endCalendar.add(Calendar.HOUR_OF_DAY, 1)
            updateTimeButtonText(isStart = false)
        }

        // Bitiş zamanı, başlangıçtan sonra mı diye son bir mantık kontrolü yap.
        if (!endCalendar.after(startCalendar)) {
            Toast.makeText(this, "Bitiş zamanı, başlangıç zamanından sonra olmalıdır.", Toast.LENGTH_SHORT).show()
            return // Bu da kritik bir kontrol, burada da dur.
        }

        // 4. Verileri Topla (Tüm kontrollerden başarıyla geçildi)
        val eventLocation = binding.eventLocationEdittext.text.toString().trim()
        val description = binding.eventDescriptionEdittext.text.toString().trim()
        val maxParticipants = maxParticipantsText.toInt() // Kontrol edildiği için artık güvenle çevirebiliriz.
        val isPrivate = if (binding.privateEventSwitch.isChecked) 1 else 0
        val password = if (isPrivate == 1) binding.eventPasswordEdittext.text.toString() else null

        val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val startTimeString = serverDateFormat.format(startCalendar.time)
        val endTimeString = serverDateFormat.format(endCalendar.time)

        val userId = PreferenceManager.getUserId()
        if (userId == null || userId == -1) {
            Toast.makeText(this, "Oturum hatası: Kullanıcı ID bulunamadı.", Toast.LENGTH_SHORT).show()
            return
        }

        // 5. ViewModel'i Çağır
        if (editModeEventId != -1) {
            createEventViewModel.updateEvent(
                eventId = editModeEventId,
                eventName = eventName,
                eventLocation = eventLocation.ifEmpty { null },
                startTime = startTimeString,
                endTime = endTimeString,
                description = description.ifEmpty { null },
                maxParticipants = maxParticipants,
                isPrivate = isPrivate,
                password = password
            )
        } else {
            createEventViewModel.createEvent(
                creatorId = userId,
                eventName = eventName,
                eventLocation = eventLocation.ifEmpty { null },
                startTime = startTimeString,
                endTime = endTimeString,
                description = description.ifEmpty { null },
                maxParticipants = maxParticipants,
                isPrivate = isPrivate,
                password = password
            )
        }
    }



    private fun showDatePicker(isStart: Boolean) {
        val calendar = if (isStart) startCalendar else endCalendar
        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateButtonText(isStart)
            if (isStart) isStartDateSet = true else isEndDateSet = true
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    private fun showTimePicker(isStart: Boolean) {
        val calendar = if (isStart) startCalendar else endCalendar
        val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            updateTimeButtonText(isStart)
            if (isStart) isStartTimeSet = true else isEndTimeSet = true
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
        timePickerDialog.show()
    }

    private fun updateDateButtonText(isStart: Boolean) {
        val calendar = if (isStart) startCalendar else endCalendar
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val button = if (isStart) binding.startDateButton else binding.endDateButton
        button.text = sdf.format(calendar.time)
    }

    private fun updateTimeButtonText(isStart: Boolean) {
        val calendar = if (isStart) startCalendar else endCalendar
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val button = if (isStart) binding.startTimeButton else binding.endTimeButton
        button.text = sdf.format(calendar.time)
    }
}