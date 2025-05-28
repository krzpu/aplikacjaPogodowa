package com.example.aplikacjapogodowa

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class szczegoly : ComponentActivity() {

    // Deklaracja pól dla widoków
    private lateinit var temperatureTextView: TextView
    private lateinit var cloudTextView: TextView
    private lateinit var wilgotnosc: TextView
    private lateinit var addToFavoritesButton: Button
    private lateinit var cityNameTextView: TextView
    private lateinit var weatherIconImageView: ImageView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // Klucz API do OpenWeatherMap
    private val apiKey: String = "e459e860dbcea1223df189b4fdd78ace"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_szczegoly)

        // Ustawienie animacji tła
        val layout = findViewById<LinearLayout>(R.id.detailLayout)
        val animationDrawable = layout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(3000)
        animationDrawable.setExitFadeDuration(3000)
        animationDrawable.start()

        // Inicjalizacja FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Przypisanie widoków do pól
        cityNameTextView = findViewById(R.id.cityNameTextView)
        temperatureTextView = findViewById(R.id.temperatureTextView)
        cloudTextView = findViewById(R.id.cloudTextView)
        wilgotnosc = findViewById(R.id.humidityTextView)
        addToFavoritesButton = findViewById(R.id.addToFavoritesButton)
        weatherIconImageView = findViewById(R.id.weatherIconImageView)

        // Inicjalizacja obiektu Ulubione, gdybyśmy chcieli dodawać do ulubionych
        val ulubione = Ulubione(this)

        var miasto : String

        // Pokazanie danych podczas ładowania
        runOnUiThread {
            cityNameTextView.text = "Ładowanie danych pogodowych"
            temperatureTextView.text = ""
            cloudTextView.text = ""
            wilgotnosc.text = ""
        }

        // PROGNOZA POGODY DLA PRZEKAZANEGO MIASTA
        if (intent.getStringExtra("CITY_NAME") != null && intent.getStringExtra("GPS") == null) {
            //Toast.makeText(this, "Prognoza dla konkretnego miasta", Toast.LENGTH_SHORT).show()

            miasto = intent.getStringExtra("CITY_NAME") ?: ""
            pobierzPogode(miasto)
            dodajDoUlubionych(miasto, ulubione)

        // PROGNOZA POGODY PRZEZ GPS
        } else if (intent.getStringExtra("GPS") != null) {
            //Toast.makeText(this, "Opcja z koordynatami", Toast.LENGTH_SHORT).show()

            pobierzPogode("")
            //dodajDoUlubionych(cityNameTextView.text.toString(), ulubione)
        }

        // Obsługa przycisku dodawania do ulubionych
        addToFavoritesButton.setOnClickListener {

            val miasto = cityNameTextView.text.toString()

            if (!ulubione.czyJestUlubione(miasto)) {
                ulubione.dodajUlubione(miasto)
                Toast.makeText(this, "$miasto dodano do ulubionych", Toast.LENGTH_SHORT).show()
            } else {
                ulubione.usunUlubione(miasto)
                Toast.makeText(this, "$miasto usunięto z ulubionych", Toast.LENGTH_SHORT).show()
            }
            // Aktualizacja tekstu przycisku po akcji
            dodajDoUlubionych(cityNameTextView.text.toString(), ulubione)
        }
    }

    // Funkcja do aktualizacji tekstu przycisku w zależności od tego, czy miasto jest ulubione
    private fun dodajDoUlubionych(miasto: String, ulubione: Ulubione) {
        if (ulubione.czyJestUlubione(miasto)) {
            addToFavoritesButton.text = "Usuń z ulubionych"
        } else {
            addToFavoritesButton.text = "Dodaj do ulubionych"
        }
    }

    // Funkcja do pobierania danych pogodowych z API OpenWeatherMap
    fun pobranieDanychZAPI(city: String, latitude: String = "", longitude: String = ""): String? {

        var urlString: String

        // Sprawdzenie, czy podano miasto, jeśli nie, używamy koordynatów podanych jako argumenty
        if (city == ""){
            urlString = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&lang=pl&appid=$apiKey"
        } else {
            urlString = "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&lang=pl&appid=$apiKey"
        }

        // Wywołanie funkcji do pobrania danych z API
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                return response.toString()

            } else {
                "Error: $responseCode"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Funkcja do pobierania pogody na podstawie miasta lub koordynatów GPS
    fun pobierzPogode(city: String) {

        // Sprawdzenie czy aplikacja ma uprawnienia do lokalizacji
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        // Jeśli uprawnienia są przyznane, pobieramy lokalizację
        else {
            val locationRequest = com.google.android.gms.location.LocationRequest
                .Builder(1000)
                .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdates(1)
                .build()

            // Callback do obsługi wyników lokalizacji (szerokość i długość geograficzna)
            val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        val latitude = location.latitude.toString()
                        val longitude = location.longitude.toString()
//                        Toast.makeText(
//                            this@szczegoly,
//                            "Lat: $latitude, Lon: $longitude",
//                            Toast.LENGTH_LONG
//                        ).show()

                        // Nowy wątek na którym pobieramy dane pogodowe
                        Thread {

                            // wywołujemy funkcję do pobrania danych z API
                            val weatherResult = pobranieDanychZAPI(city, latitude, longitude)

                            // Uzupełnianie dancyh pogodowych w widokach
                            runOnUiThread {
                                try {

                                    // Parsowanie JSON-a i wyciąganie danych pogodowych
                                    val jsonResponse = JSONObject(weatherResult ?: "")

                                    val main = jsonResponse.getJSONObject("main")

                                    val temperature = main.getDouble("temp")
                                    val humidity = main.getInt("humidity")
                                    val weatherArray = jsonResponse.getJSONArray("weather")
                                    val weatherDescription = weatherArray.getJSONObject(0).getString("description")
                                    val cityName = jsonResponse.getString("name")


                                    // Aktualizacja widoków z danymi pogodowymi
                                    temperatureTextView.text = "$temperature°C"
                                    cloudTextView.text = "$weatherDescription"
                                    wilgotnosc.text = "$humidity%"
                                    cityNameTextView.text = "$cityName"

                                    //dodajDoUlubionych(cityNameTextView.text.toString(), ulubione)

                                    // Kolorowanie temperatury
                                    val color = when {
                                        temperature < 10 -> android.graphics.Color.BLUE
                                        temperature in 10.0..20.0 -> android.graphics.Color.BLACK
                                        else -> android.graphics.Color.RED
                                    }
                                    temperatureTextView.setTextColor(color)
                                } catch (e: Exception) {
                                    Log.e("WeatherAPI", "Blad parsowania JSON: ${e.localizedMessage}")
                                    Toast.makeText(this@szczegoly, "Błąd podczas parsowania danych pogodowych", Toast.LENGTH_SHORT).show()
                                }
                            }

                            // Pobieranie ikony pogody
                            try {
                                val jsonResponse = JSONObject(weatherResult ?: "")
                                val weatherArray = jsonResponse.getJSONArray("weather")
                                val weatherIcon = weatherArray.getJSONObject(0).getString("icon")
                                val iconUrl = "https://openweathermap.org/img/wn/${weatherIcon}@2x.png"

                                // Pobieranie bitmapy i ustawianie obrazka
                                Thread {
                                    try {
                                        val input = URL(iconUrl).openStream()
                                        val bitmap = BitmapFactory.decodeStream(input)
                                        runOnUiThread {
                                            weatherIconImageView.setImageBitmap(bitmap)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }.start()
                            } catch (e: Exception) {
                                Log.e("WeatherAPI", "Blad parsowania JSON: ${e.localizedMessage}")
                                Toast.makeText(this@szczegoly, "Błąd podczas parsowania danych pogodowych", Toast.LENGTH_SHORT).show()
                            }

                        }.start()

                    } else {
                        Toast.makeText(this@szczegoly, "Lokalizacja niedostepna", Toast.LENGTH_SHORT).show()
                    }
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )


        }
    }
}