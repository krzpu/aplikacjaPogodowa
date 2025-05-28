package com.example.aplikacjapogodowa

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
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

    private lateinit var temperatureTextView: TextView
    private lateinit var cloudTextView: TextView
    private lateinit var rainTextView: TextView
    private lateinit var addToFavoritesButton: Button
    private lateinit var cityNameTextView: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private val apiKey: String = "e459e860dbcea1223df189b4fdd78ace"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_szczegoly)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        cityNameTextView = findViewById(R.id.cityNameTextView)
        temperatureTextView = findViewById(R.id.temperatureTextView)
        cloudTextView = findViewById(R.id.cloudTextView)
        rainTextView = findViewById(R.id.rainTextView)
        addToFavoritesButton = findViewById(R.id.addToFavoritesButton)

        val ulubione = Ulubione(this)

        // Pokazanie danych podczas ładowania
        runOnUiThread {
            cityNameTextView.text = "Ładowanie danych pogodowych..."
            temperatureTextView.text = ""
            cloudTextView.text = ""
            rainTextView.text = ""
        }

        // PROGNOZA POGODY DLA PRZEKAZANEGO MIASTA
        if (intent.getStringExtra("CITY_NAME") != null && intent.getStringExtra("GPS") == null) {
            Toast.makeText(this, "Prognoza dla konkretnego miasta", Toast.LENGTH_SHORT).show()

            val miasto = intent.getStringExtra("CITY_NAME") ?: ""
            cityNameTextView.text = "$miasto"

            pobierzPogodeDlaKoordynatow(miasto)
            // Ustaw tekst przycisku na start
            updateFavoriteButton(miasto, ulubione)


        // PROGNOZA POGODY PRZEZ GPS
        } else if (intent.getStringExtra("GPS") != null) {
            Toast.makeText(this, "Opcja z koordynatami", Toast.LENGTH_SHORT).show()

            pobierzKoordynaty()
            updateFavoriteButton(cityNameTextView.text.toString(), ulubione)
        }

        
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
            updateFavoriteButton(cityNameTextView.text.toString(), ulubione)
        }
    }

    private fun updateFavoriteButton(miasto: String, ulubione: Ulubione) {
        if (ulubione.czyJestUlubione(miasto)) {
            addToFavoritesButton.text = "Usuń z ulubionych"
        } else {
            addToFavoritesButton.text = "Dodaj do ulubionych"
        }
    }

    fun pobierzPogodeDlaKoordynatow(city: String, latitude: String = "", longitude: String = ""): String? {

        var urlString: String

        if (city == ""){
            urlString = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&lang=pl&appid=$apiKey"
        } else {
            urlString = "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&lang=pl&appid=$apiKey"
        }

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

    fun pobierzKoordynaty() {
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
        } else {
            val locationRequest = com.google.android.gms.location.LocationRequest
                .Builder(1000)
                .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdates(1)
                .build()

            val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        val latitude = location.latitude.toString()
                        val longitude = location.longitude.toString()
                        Toast.makeText(
                            this@szczegoly,
                            "Lat: $latitude, Lon: $longitude",
                            Toast.LENGTH_LONG
                        ).show()

                        Thread {
                            val weatherResult = pobierzPogodeDlaKoordynatow("", latitude, longitude)

                            Log.d("WeatherAPI", "Received: $weatherResult")

                            runOnUiThread {
                                try {
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
                                    rainTextView.text = "$humidity%"
                                    cityNameTextView.text = "$cityName"


                                } catch (e: Exception) {
                                    Log.e("WeatherAPI", "Failed to parse JSON: ${e.localizedMessage}")
                                    Toast.makeText(this@szczegoly, "Błąd podczas parsowania danych pogodowych", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.start()

                    } else {
                        Toast.makeText(this@szczegoly, "Location not available", Toast.LENGTH_SHORT).show()
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