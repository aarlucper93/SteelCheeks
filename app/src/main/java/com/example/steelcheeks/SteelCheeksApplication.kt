package com.example.steelcheeks

import android.app.Application
import com.example.steelcheeks.data.database.FoodRoomDatabase
import com.jakewharton.threetenabp.AndroidThreeTen

class SteelCheeksApplication : Application() {
    //Lazily instantiate the database instance
    val database: FoodRoomDatabase by lazy { FoodRoomDatabase.getDatabase(this)}

    override fun onCreate() {
        super.onCreate()
        //Permite el uso de LocalDate con versiones de API inferiores a 26
        AndroidThreeTen.init(this)
    }
}