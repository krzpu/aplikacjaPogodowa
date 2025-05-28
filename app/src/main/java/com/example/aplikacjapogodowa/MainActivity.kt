package com.example.aplikacjapogodowa

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    // Deklaracja pól dla widoków
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var gpsButton: Button
    private lateinit var listaUlubionych: ListView

    // Trzymamy adapter jako pole, by móc go odświeżać przy zmianach ekranu
    private lateinit var favoritesAdapter: ArrayAdapter<String>
    private lateinit var ulubione: Ulubione

    // główna funkcja onCreate, która jest wywoływana przy tworzeniu aktywności
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ustawienie animacji tła
        val layout = findViewById<LinearLayout>(R.id.rootLayout)
        val animationDrawable = layout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(3000)
        animationDrawable.setExitFadeDuration(3000)
        animationDrawable.start()

        // przypisanie widoków do pól
        searchEditText    = findViewById(R.id.searchEditText)
        searchButton      = findViewById(R.id.searchButton)
        gpsButton         = findViewById(R.id.gpsButton)
        listaUlubionych   = findViewById(R.id.ulubioneListView)

        ulubione = Ulubione(this)

        // Adapter na start z pustą listą
        favoritesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listaUlubionych.adapter = favoritesAdapter

        // Przejście do szczegółów po kliknięciu na element listy
        listaUlubionych.setOnItemClickListener { _, _, position, _ ->
            val miasto = favoritesAdapter.getItem(position) ?: return@setOnItemClickListener
            otworzSzczegoly(miasto)
        }

        // Przejście do szczegółów po wpisaniu nazwy miasta
        searchButton.setOnClickListener {
            val miasto = searchEditText.text.toString().trim()
            if (miasto.isNotEmpty()) {
                otworzSzczegoly(miasto)
            } else {
                Toast.makeText(this, "Wpisz nazwę miasta", Toast.LENGTH_SHORT).show()
            }
        }

        // Przycisk GPS do otwarcia szczegółów bez wpisywania miasta
        gpsButton.setOnClickListener {
            otworzSzczegoly()
        }
    }

    // Aktywność jest wznawiana, gdy wracamy do niej z innej aktywnośc
    override fun onResume() {
        super.onResume()
        // Za każdym razem, gdy wracamy do tego ekranu, odświeżamy listę ulubionych
        zaladujUlubione()
    }

    // Metoda do załadowania ulubionych miast z klasy Ulubione
    private fun zaladujUlubione() {
        val lista = ulubione.getUlubione().toList()
        favoritesAdapter.clear()
        favoritesAdapter.addAll(lista)
        favoritesAdapter.notifyDataSetChanged()
    }

    //
    private fun otworzSzczegoly(miasto: String) {
        val intent = Intent(this, szczegoly::class.java)
        intent.putExtra("CITY_NAME", miasto)
        startActivity(intent)
    }

    // Metoda do otwarcia szczegółów bez podawania nazwy miasta - przez GPS
    private fun otworzSzczegoly() {
        val intent = Intent(this, szczegoly::class.java)
        intent.putExtra("GPS", "")
        startActivity(intent)
    }

}