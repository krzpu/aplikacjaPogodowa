package com.example.aplikacjapogodowa

import android.content.Context
import android.content.SharedPreferences

class Ulubione(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("ulubione_prefs", Context.MODE_PRIVATE)

    fun getUlubione(): Set<String> {
        return prefs.getStringSet("ulubione", emptySet()) ?: emptySet()
    }

    fun dodajUlubione(miasto: String) {
        val ulubione = getUlubione().toMutableSet()
        ulubione.add(miasto)
        prefs.edit().putStringSet("ulubione", ulubione).apply()
    }

    fun usunUlubione(miasto: String) {
        val ulubione = getUlubione().toMutableSet()
        ulubione.remove(miasto)
        prefs.edit().putStringSet("ulubione", ulubione).apply()
    }

    fun czyJestUlubione(miasto: String): Boolean {
        return getUlubione().contains(miasto)
    }
}