package com.example.steelcheeks

import android.app.Application
import com.example.steelcheeks.database.FoodRoomDatabase

class SteelCheeksApplication : Application() {

    //Lazily instantiate the database instance
    val database: FoodRoomDatabase by lazy {FoodRoomDatabase.getDatabase(this)}
}