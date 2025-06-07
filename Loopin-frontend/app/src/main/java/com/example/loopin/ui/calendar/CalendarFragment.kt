package com.example.loopin.ui.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.loopin.PreferenceManager // Kullanılıyorsa kalsın
import com.example.loopin.R
import com.example.loopin.databinding.CalendarDayTitleTextBinding // Gün başlıkları için
import com.example.loopin.databinding.FragmentCalendarBinding
import com.example.loopin.network.ApiClient // Kullanılıyorsa kalsın
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private var selectedDay: CalendarDay? = null
    private var events: List<Pair<String, CalendarDay>> = emptyList()

    // Ay ve Yıl başlığı için formatlayıcı
    // Locale.getDefault() yerine istediğiniz bir Locale kullanabilirsiniz, örneğin Locale("tr")
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        val underline: View = view.findViewById(R.id.underline) // calendar_day_layout.xml'de bu ID olmalı
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendarView()
        setupDayTitles()
        loadEvents() // Etkinlikleri yükle

        // Ay ve Yıl Navigasyon Butonları için Listener'lar
        binding.nextMonthButton.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.plusMonths(1))
            }
        }

        binding.previousMonthButton.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.minusMonths(1))
            }
        }
    }

    private fun setupCalendarView() {
        val currentMonth = YearMonth.now()
        // Takvim aralığını ihtiyacınıza göre ayarlayın
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        // Locale'e göre haftanın ilk gününü al (Pazartesi, Pazar vb.)
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        // CalendarView'ı ayarla
        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        // Mevcut aya kaydır
        binding.calendarView.scrollToMonth(currentMonth)

        // Ay kaydırıldığında ay/yıl başlığını güncelle
        binding.calendarView.monthScrollListener = { calendarMonth: CalendarMonth ->
            updateMonthYearText(calendarMonth.yearMonth)
        }

        // Günleri bağlamak için binder
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view) // calendar_day_layout.xml'i inflate eder
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                bindDayView(container, data)
            }
        }
        // Başlangıçta ay/yıl başlığını ayarla
        updateMonthYearText(currentMonth)
    }

    private fun updateMonthYearText(yearMonth: YearMonth) {
        // XML'de tanımladığınız monthYearText TextView'ını güncelle
        binding.monthYearText.text = monthTitleFormatter.format(yearMonth)
    }

    private fun bindDayView(container: DayViewContainer, day: CalendarDay) {
        container.textView.text = day.date.dayOfMonth.toString()

        if (day.position == DayPosition.MonthDate) { // Sadece mevcut ayın günleri için işlem yap
            container.textView.visibility = View.VISIBLE // Görünür yap
            container.underline.visibility = View.INVISIBLE // Başlangıçta alt çizgiyi gizle

            val hasEvent = events.any { it.second.date == day.date } // Sadece tarih kısmını karşılaştır
            container.underline.visibility = if (hasEvent) View.VISIBLE else View.INVISIBLE

            if (selectedDay?.date == day.date) { // Sadece tarih kısmını karşılaştır
                container.textView.setBackgroundResource(R.drawable.selected_day_bg)
                // container.textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // Örnek
            } else {
                container.textView.background = null
                // container.textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.default_text_color)) // Örnek
            }
            container.view.setOnClickListener { onDayClicked(day) }
        } else { // Ay dışındaki günler
            container.textView.visibility = View.INVISIBLE // Gizle veya soluklaştır
            container.underline.visibility = View.INVISIBLE
            container.textView.background = null
            container.view.setOnClickListener(null) // Tıklanabilirliği kaldır
        }
    }

    private fun setupDayTitles() {
        val daysOfWeek = daysOfWeek(firstDayOfWeek = firstDayOfWeekFromLocale())
        val titlesLayout = binding.titlesContainer.daysContainer // Bu CalendarDayTitlesContainerBinding üzerinden gelen LinearLayout

        Log.d("CalendarFragment", "titlesLayout child count: ${titlesLayout.childCount}, daysOfWeek size: ${daysOfWeek.size}")

        if (titlesLayout.childCount == daysOfWeek.size) {
            titlesLayout.children.forEachIndexed { index, childView ->
                Log.d("CalendarFragment", "ChildView at index $index: ${childView::class.java.name}, Tag: ${childView.tag}") // LOG EKLEYİN
                try {
                    // Deneme 1: View'ı doğrudan logla
                    Log.d("CalendarFragment", "Attempting to bind childView: $childView")

                    // Deneme 2: childView'ın ID'sini logla (eğer bir ID'si varsa)
                    // try {
                    //     Log.d("CalendarFragment", "ChildView ID: ${childView.id}, Resources Name: ${if (childView.id != View.NO_ID) resources.getResourceEntryName(childView.id) else "NO_ID"}")
                    // } catch (e: Exception) {
                    //     Log.d("CalendarFragment", "Could not get resource name for childView ID.")
                    // }

                    val itemBinding = CalendarDayTitleTextBinding.bind(childView) // Hata burada oluşuyor
                    itemBinding.dayTitleText.text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    Log.d("CalendarFragment", "Successfully bound and set text for index $index")
                } catch (e: RuntimeException) { // Özellikle RuntimeException'ı yakala
                    Log.e("CalendarFragment", "RuntimeException binding day title at index $index: ${e.message}", e)
                    // Hata durumunda alternatif
                    val textView = childView as? TextView
                    if (textView != null) {
                        textView.text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        Log.d("CalendarFragment", "Fallback: Set text directly to TextView for index $index")
                    } else {
                        Log.e("CalendarFragment", "Fallback failed: childView is not a TextView at index $index")
                    }
                } catch (e: Exception) {
                    Log.e("CalendarFragment", "Generic Exception binding day title at index $index: ${e.message}", e)
                }
            }
        } else {
            Log.e("CalendarFragment", "Day title view count (${titlesLayout.childCount}) does not match days of week count (${daysOfWeek.size}). Ensure calendar_day_titles_container.xml has the correct number of includes.")
        }
    }

    private fun loadEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                events = getAllParticipatedEvents()
                binding.calendarView.notifyCalendarChanged() // Takvimi yenile
            } catch (e: Exception) {
                Log.e("CalendarFragment", "Error loading events: ${e.message}", e)
                Toast.makeText(context, "Error loading events", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onDayClicked(day: CalendarDay) {
        if (day.position != DayPosition.MonthDate) return // Sadece ay içindeki günlere tıklanabilsin

        val oldSelectedDay = selectedDay
        selectedDay = if (selectedDay?.date == day.date) null else day // Tekrar tıklayınca seçimi kaldır

        // Önceki ve yeni seçili günleri güncellemek için takvime haber ver
        binding.calendarView.notifyDateChanged(day.date)
        oldSelectedDay?.let { binding.calendarView.notifyDateChanged(it.date) }

        if (selectedDay != null) {
            showEventsForDay(selectedDay!!) // selectedDay null değilse göster
        } else {
            // Seçim kaldırıldıysa Toast mesajını temizleyebilir veya başka bir işlem yapabilirsiniz.
            // Örneğin: Toast.makeText(requireContext(), "Date selection cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEventsForDay(day: CalendarDay) {
        // Etkinlikleri filtrelerken sadece tarih kısmını karşılaştır
        val selectedEvents = events.filter { it.second.date == day.date }
        val message = if (selectedEvents.isNotEmpty()) {
            val eventNames = selectedEvents.joinToString(", ") { it.first }
            "Events on ${day.date.dayOfMonth}/${day.date.monthValue}: $eventNames"
        } else {
            "No events for ${day.date.dayOfMonth}/${day.date.monthValue}"
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private suspend fun getAllParticipatedEvents(): List<Pair<String, CalendarDay>> {
        fun eventToCalendarDay(eventStartTime: String): CalendarDay {
            // ISO_OFFSET_DATE_TIME, "2011-12-03T10:15:30+01:00" gibi offset'leri ve "Z" (UTC) yi anlar
            val odt = OffsetDateTime.parse(eventStartTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            return CalendarDay(odt.toLocalDate(), DayPosition.MonthDate)
        }

        val userId = PreferenceManager.getUserId() // Context eklendi
        val allEvents = mutableListOf<Pair<String, CalendarDay>>()
        var currentPage = 1
        var totalPages: Int

        // getEventsUserParticipates
        do {
            if (userId == null) break // userId yoksa döngüye girme
            try {
                val response = ApiClient.eventApi.getEventsUserParticipates(userId, currentPage)
                if (response.isSuccessful && response.body() != null) {
                    val eventResponse = response.body()!!
                    for (event in eventResponse.events) {
                        val calendarDay = eventToCalendarDay(event.startTime)
                        allEvents.add(event.eventName to calendarDay)
                    }
                    totalPages = eventResponse.pagination.totalPages
                    if (currentPage >= totalPages) break // Son sayfaya ulaşıldıysa çık
                } else {
                    Log.e("CalendarFragment", "getEventsUserParticipates API error: ${response.code()} - ${response.message()}")
                    break // Hata durumunda döngüden çık
                }
            } catch (e: Exception) {
                Log.e("CalendarFragment", "getEventsUserParticipates API call failed: ${e.message}", e)
                break // İstisna durumunda döngüden çık
            }
            currentPage++
        } while (true) // Koşul yukarıda break ile yönetiliyor

        currentPage = 1

        // getUpcomingEventsUserParticipates
        do {
            if (userId == null) break
            try {
                val response = ApiClient.eventApi.getUpcomingEventsUserParticipates(userId, currentPage)
                if (response.isSuccessful && response.body() != null) {
                    val eventResponse = response.body()!!
                    for (event in eventResponse.events) {
                        val calendarDay = eventToCalendarDay(event.startTime)
                        allEvents.add(event.eventName to calendarDay)
                    }
                    totalPages = eventResponse.pagination.totalPages
                    if (currentPage >= totalPages) break
                } else {
                    Log.e("CalendarFragment", "getUpcomingEventsUserParticipates API error: ${response.code()} - ${response.message()}")
                    break
                }
            } catch (e: Exception) {
                Log.e("CalendarFragment", "getUpcomingEventsUserParticipates API call failed: ${e.message}", e)
                break
            }
            currentPage++
        } while (true)

        return allEvents.distinctBy { it.first to it.second.date } // Aynı etkinliğin (isim ve tarih) tekrarlarını kaldır
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Bellek sızıntılarını önlemek için
    }
}