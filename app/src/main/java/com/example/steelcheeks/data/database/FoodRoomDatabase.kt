package com.example.steelcheeks.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.steelcheeks.data.database.diary.DiaryDao
import com.example.steelcheeks.data.database.diary.DiaryEntryEntity
import com.example.steelcheeks.data.database.food.FoodDao
import com.example.steelcheeks.data.database.food.FoodEntity


@Database(entities = [FoodEntity::class, DiaryEntryEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FoodRoomDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun diaryDao(): DiaryDao

    companion object {

        @Volatile
        private var INSTANCE: FoodRoomDatabase? = null

        fun getDatabase(context: Context): FoodRoomDatabase {
            //Return the database instance if it exists. Otherwise, build it.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoodRoomDatabase::class.java,
                    "food_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}