package com.example.aplikacjapogodowa

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class szczegoly : ComponentActivity() {

    private lateinit var temperatureTextView: TextView
    private lateinit var cloudTextView: TextView
    private lateinit var rainTextView: TextView
    private lateinit var addToFavoritesButton: Button
    private lateinit var cityNameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_szczegoly)

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
}