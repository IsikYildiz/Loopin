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
import com.example.loopin.PreferenceManager
import com.example.loopin.R
import com.example.loopin.databinding.CalendarDayTitleTextBinding
import com.example.loopin.databinding.FragmentCalendarBinding
import com.example.loopin.network.ApiClient
import com.example.loopin.network.OpenMeteoApiClient
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
import java.time.LocalDate
import androidx.appcompat.app.AlertDialog // AlertDialog için import
import java.time.format.FormatStyle

class CalendarFragment : Fragment() {
    private var lastWeatherFetchTime: Long = 0L
    private val weatherFetchCooldownMillis: Long = 12 * 1000 // 12 saniye
    private var lastFetchedLocation: String? = null
    private var lastFetchedWeatherForDay: Pair<LocalDate, String>? = null

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private var selectedDay: CalendarDay? = null
    private var events: List<Pair<String, CalendarDay>> = emptyList()

    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        val underline: View = view.findViewById(R.id.underline)
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
        loadEvents()
        setupMonthNavigation()
    }

    private fun setupMonthNavigation() {
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
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)

        binding.calendarView.monthScrollListener = { calendarMonth: CalendarMonth ->
            updateMonthYearText(calendarMonth.yearMonth)
        }

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                bindDayView(container, data)
            }
        }
        updateMonthYearText(currentMonth)
    }

    private fun updateMonthYearText(yearMonth: YearMonth) {
        binding.monthYearText.text = monthTitleFormatter.format(yearMonth)
    }

    private fun bindDayView(container: DayViewContainer, day: CalendarDay) {
        container.textView.text = day.date.dayOfMonth.toString()

        if (day.position == DayPosition.MonthDate) {
            container.textView.visibility = View.VISIBLE
            container.underline.visibility = if (events.any { it.second.date == day.date }) View.VISIBLE else View.INVISIBLE

            container.textView.setBackgroundResource(
                if (selectedDay?.date == day.date) R.drawable.selected_day_bg else 0 // 0 for no background
            )
            container.view.setOnClickListener { onDayClicked(day) }
        } else {
            container.textView.visibility = View.INVISIBLE
            container.underline.visibility = View.INVISIBLE
            container.textView.background = null
            container.view.setOnClickListener(null)
        }
    }

    private fun setupDayTitles() {
        val daysOfWeek = daysOfWeek(firstDayOfWeek = firstDayOfWeekFromLocale())
        val titlesLayout = binding.titlesContainer.daysContainer
        if (titlesLayout.childCount == daysOfWeek.size) {
            titlesLayout.children.forEachIndexed { index, childView ->
                try {
                    CalendarDayTitleTextBinding.bind(childView).dayTitleText.text =
                        daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.getDefault())
                } catch (e: Exception) {
                    Log.e("CalendarFragment", "Error binding day title at index $index: ${e.message}", e)
                    (childView as? TextView)?.text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.getDefault())
                }
            }
        } else {
            Log.e("CalendarFragment", "Day title view count mismatch.")
        }
    }

    private fun loadEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            val userId = PreferenceManager.getUserId()
            if (userId != null) {
                try {
                    events = getAllParticipatedEvents(userId)
                    binding.calendarView.notifyCalendarChanged()
                } catch (e: Exception) {
                    Log.e("CalendarFragment", "Error loading events: ${e.message}", e)
                    Toast.makeText(context, "Error loading events", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.w("CalendarFragment", "User ID not found, cannot load events.")
                // Toast.makeText(context, "User not logged in, cannot load events.", Toast.LENGTH_SHORT).show() // İsteğe bağlı
                events = emptyList()
                binding.calendarView.notifyCalendarChanged()
            }
        }
    }

    private fun onDayClicked(day: CalendarDay) {
        if (day.position != DayPosition.MonthDate) return

        val oldSelectedDay = selectedDay
        selectedDay = if (selectedDay?.date == day.date) null else day

        binding.calendarView.notifyDateChanged(day.date)
        oldSelectedDay?.let { binding.calendarView.notifyDateChanged(it.date) }

        if (selectedDay != null) {
            val currentSelectedDay = selectedDay!! // Artık null değil
            viewLifecycleOwner.lifecycleScope.launch {
                val userId = PreferenceManager.getUserId()
                if (userId == null) {
                    showEventAndWeatherDetailsDialog(currentSelectedDay, "User ID not found. Cannot fetch location.")
                    return@launch
                }
                try {
                    val response = ApiClient.userApi.getUserProfile(userId)
                    if (response.isSuccessful && response.body() != null) {
                        val userLocation = response.body()!!.user?.location
                        if (userLocation != null) {
                            fetchAndShowWeatherForDay(currentSelectedDay, userLocation) // Sadece CalendarDay geç
                        } else {
                            showEventAndWeatherDetailsDialog(currentSelectedDay, "Location not found in user profile.")
                        }
                    } else {
                        Log.e("CalendarFragment", "Error getting user profile: ${response.code()} - ${response.message()}")
                        showEventAndWeatherDetailsDialog(currentSelectedDay, "Failed to get user profile.")
                    }
                } catch (e: Exception) {
                    Log.e("CalendarFragment", "Exception getting user profile: ${e.message}", e)
                    showEventAndWeatherDetailsDialog(currentSelectedDay, "Error fetching user profile.")
                }
            }
        }
    }


    private fun fetchAndShowWeatherForDay(calendarDay: CalendarDay, locationName: String) {
        val selectedDate = calendarDay.date // CalendarDay'den LocalDate al
        val currentTime = System.currentTimeMillis()

        // 1. Önbellek kontrolü (aynı gün, aynı konum, cooldown süresi dolmamış)
        if (lastFetchedLocation == locationName &&
            lastFetchedWeatherForDay?.first == selectedDate &&
            (currentTime - lastWeatherFetchTime) < weatherFetchCooldownMillis
        ) {
            showEventAndWeatherDetailsDialog(calendarDay, lastFetchedWeatherForDay!!.second)
            return
        }

        // 2. Cooldown genel kontrolü (farklı gün veya konum olsa bile, son istekten bu yana yeterli süre geçmemişse)
        if ((currentTime - lastWeatherFetchTime) < weatherFetchCooldownMillis && lastFetchedLocation != null) {
            val remainingSeconds = (weatherFetchCooldownMillis - (currentTime - lastWeatherFetchTime)) / 1000
            val cooldownMessage = "Please try again in ${remainingSeconds + 1} seconds"
            // Cooldown durumunda, en son başarılı hava durumu bilgisini (varsa) veya cooldown mesajını göster
            val messageToShow = lastFetchedWeatherForDay?.takeIf { it.first == selectedDate }?.second ?: cooldownMessage
            showEventAndWeatherDetailsDialog(calendarDay, messageToShow)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val coordinates = LocationGeocoder.getCoordinatesFromLocationName(requireContext(), locationName)

                if (coordinates != null) {
                    val lat = coordinates.first
                    val lon = coordinates.second
                    Log.d("WeatherFetch", "Geocoded '$locationName' to Lat: $lat, Lon: $lon for date: $selectedDate")

                    val dateString = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val weatherApiResponse = OpenMeteoApiClient.weatherApi.getCurrentWeather(
                        latitude = lat,
                        longitude = lon,
                        startDate = dateString,
                        endDate = dateString
                    )

                    if (weatherApiResponse.isSuccessful && weatherApiResponse.body() != null) {
                        val dailyData = weatherApiResponse.body()!!.daily
                        if (dailyData?.time?.isNotEmpty() == true) {
                            val maxTemp = dailyData.temperature2mMax?.getOrNull(0)
                            val minTemp = dailyData.temperature2mMin?.getOrNull(0)
                            val precipitation = dailyData.precipitationSum?.getOrNull(0)
                            val weatherCode = dailyData.weathercode?.getOrNull(0)

                            val weatherDescription = formatWeatherMessage(locationName, maxTemp, minTemp, precipitation, weatherCode)
                            lastWeatherFetchTime = System.currentTimeMillis()
                            lastFetchedLocation = locationName
                            lastFetchedWeatherForDay = selectedDate to weatherDescription
                            showEventAndWeatherDetailsDialog(calendarDay, weatherDescription)
                        } else {
                            // API'den veri geldi ama 'daily' kısmı boş veya hatalı
                            Log.w("WeatherFetch", "Daily weather data is missing or empty from API for $selectedDate")
                            handleWeatherError(calendarDay, "Could not find weather information")
                        }
                    } else {
                        Log.e("WeatherFetch", "Forecast API error: ${weatherApiResponse.code()} - ${weatherApiResponse.message()}")
                        handleWeatherError(calendarDay, "Could not find weather information")
                    }
                } else {
                    Log.e("WeatherFetch", "Geocoder could not find coordinates for '$locationName'")
                    handleWeatherError(calendarDay, "Could not find weather information") // Konum bulunamayınca da aynı mesaj
                }
            } catch (e: Exception) {
                Log.e("WeatherFetch", "Exception during weather fetch: ${e.message}", e)
                handleWeatherError(calendarDay, "Could not find weather information") // Genel hata için de aynı mesaj
            }
        }
    }


    private fun formatWeatherMessage(
        location: String,
        maxTemp: Double?,
        minTemp: Double?,
        precipitation: Double?,
        weatherCode: Int?
    ): String {
        val tempString = if (maxTemp != null && minTemp != null) {
            "${minTemp.toInt()}-${maxTemp.toInt()}°C"
        } else if (maxTemp != null) {
            "${maxTemp.toInt()}°C"
        } else {
            "N/A Temp" // Sıcaklık bilgisi yoksa
        }

        val precipitationString = if (precipitation != null && precipitation > 0.0) {
            ", Rain: ${"%.1f".format(Locale.US, precipitation)}mm"
        } else if (precipitation != null) { // precipitation == 0.0 veya negatif (genelde olmaz ama)
            ", No rain" // Daha kısa
        } else {
            "" // Yağış bilgisi yok
        }

        val weatherIcon = getWeatherEmoji(weatherCode)
        return "$location: $tempString $weatherIcon$precipitationString".trim() // Sondaki boşlukları temizle
    }


    private fun getWeatherEmoji(weatherCode: Int?): String { // public olabilir, eğer başka yerden de kullanılacaksa
        return when (weatherCode) {
            0 -> "☀️"
            1, 2, 3 -> "☁️"
            45, 48 -> "🌫️"
            51, 53, 55 -> "Mizzle: 🌧️"
            56, 57 -> "Freezing Mizzle: ❄️🌧️"
            61 -> "Slight Rain: 🌧️"
            63 -> "Moderate Rain: 🌧️"
            65 -> "Heavy Rain: 🌧️"
            66, 67 -> "Freezing Rain: ❄️🌧️"
            71 -> "Slight Snow: ❄️"
            73 -> "Moderate Snow: ❄️"
            75 -> "Heavy Snow: ❄️"
            77 -> "Snow Grains: ❄️"
            80, 81, 82 -> "Rain Showers: 🌦️"
            85, 86 -> "Snow Showers: ❄️🌨️"
            95 -> "Thunderstorm: ⛈️"
            96, 99 -> "Thunderstorm with Hail: ⛈️🌪️"
            else -> "🛰️"
        }
    }

    // handleWeatherError artık sadece son önbelleği ve zamanı güncelleyip Toast'ı çağırıyor
    private fun handleWeatherError(calendarDay: CalendarDay, specificErrorMessage: String) {
        lastWeatherFetchTime = System.currentTimeMillis() // Cooldown için zamanı yine de güncelle
        // lastFetchedLocation = null; // Hata durumunda son konumu sıfırlamak opsiyonel
        lastFetchedWeatherForDay = calendarDay.date to specificErrorMessage // Hata mesajını önbelleğe al
        showEventAndWeatherDetailsDialog(calendarDay, specificErrorMessage)
    }

    private fun showEventAndWeatherDetailsDialog(day: CalendarDay, weatherInfo: String?) {
        val selectedEvents = events.filter { it.second.date == day.date }

        val eventDetails = if (selectedEvents.isNotEmpty()) {
            val eventTitles = selectedEvents.joinToString("\n") { "- ${it.first}" } // Her etkinliği yeni satırda ve madde imi ile
            "Events:\n$eventTitles"
        } else {
            "No events today."
        }

        val weatherDetails = if (weatherInfo != null && weatherInfo.isNotBlank()) {
            weatherInfo
        } else {
            ""
        }

        var dialogMessageBody = eventDetails
        if (weatherDetails.isNotBlank()) {
            dialogMessageBody += "\n\n$weatherDetails" // Etkinlikler ve hava durumu arasına boşluk koy
        }

        // Dialog başlığı için tarihi formatla
        val formattedDate = day.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

        AlertDialog.Builder(requireContext())
            .setTitle("Details for $formattedDate")
            .setMessage(dialogMessageBody.trim()) // Baştaki/sondaki boşlukları temizle
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss() // OK butonuna basılınca dialog'u kapat
            }
            .setCancelable(true) // Dialog dışına tıklanınca kapanmasını sağlar (isteğe bağlı)
            .show()
    }

    private suspend fun getAllParticipatedEvents(userId: Int): List<Pair<String, CalendarDay>> {
        fun eventToCalendarDay(eventStartTime: String): CalendarDay {
            val odt = OffsetDateTime.parse(eventStartTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            return CalendarDay(odt.toLocalDate(), DayPosition.MonthDate)
        }

        val allEvents = mutableListOf<Pair<String, CalendarDay>>()
        var currentPage = 1
        var totalPages: Int

        // getEventsUserParticipates
        do {
            try {
                val response = ApiClient.eventApi.getEventsUserParticipates(userId, currentPage)
                if (response.isSuccessful && response.body() != null) {
                    val eventResponse = response.body()!!
                    eventResponse.events.forEach { event ->
                        allEvents.add(event.eventName to eventToCalendarDay(event.startTime))
                    }
                    totalPages = eventResponse.pagination.totalPages
                    if (currentPage >= totalPages) break
                } else {
                    Log.e("CalendarFragment", "getEventsUserParticipates API error: ${response.code()} - ${response.message()}")
                    break
                }
            } catch (e: Exception) {
                Log.e("CalendarFragment", "getEventsUserParticipates API call failed: ${e.message}", e)
                break
            }
            currentPage++
        } while (true)

        currentPage = 1

        // getUpcomingEventsUserParticipates
        do {
            try {
                val response = ApiClient.eventApi.getUpcomingEventsUserParticipates(userId, currentPage)
                if (response.isSuccessful && response.body() != null) {
                    val eventResponse = response.body()!!
                    eventResponse.events.forEach { event ->
                        allEvents.add(event.eventName to eventToCalendarDay(event.startTime))
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

        return allEvents.distinctBy { it.first to it.second.date }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}