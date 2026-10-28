package com.farsanahmed.taskflowlite.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.farsanahmed.taskflowlite.database.TaskEntity

@Database(entities = [TaskEntity::class], version = 1, exportSchemas = false)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "taskflowlite-database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = db
                db
            }
        }
    }
}
