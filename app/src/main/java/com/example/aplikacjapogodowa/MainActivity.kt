package com.example.aplikacjapogodowa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    // Deklaracja pól dla widoków
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var gpsButton: Button
    private lateinit var listaUlubionych: ListView

    // główna funkcja onCreate, która jest wywoływana przy tworzeniu aktywności
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchEditText    = findViewById(R.id.searchEditText)
        searchButton      = findViewById(R.id.searchButton)
        gpsButton         = findViewById(R.id.gpsButton)
        listaUlubionych   = findViewById(R.id.ulubioneListView)


        searchButton.setOnClickListener {
            val miasto = searchEditText.text.toString().trim()
            if (miasto.isNotEmpty()) {
                val intent = Intent(this, szczegoly::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Wpisz nazwę miasta", Toast.LENGTH_SHORT).show()
            }
        }

        gpsButton.setOnClickListener {
            val intent = Intent(this, szczegoly::class.java)
            startActivity(intent)
        }


    }
}