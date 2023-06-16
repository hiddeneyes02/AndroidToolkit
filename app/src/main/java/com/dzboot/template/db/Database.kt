package com.dzboot.template.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dzboot.template.Model

private const val DATABASE = "servers"


@Database(entities = [Model::class], version = 1, exportSchema = false)
abstract class ServersDatabase : RoomDatabase() {

	abstract fun serversDao(): ServerDao

	companion object {

		// For Singleton instantiation
		@Volatile
		private var instance: ServersDatabase? = null

		fun getInstance(context: Context): ServersDatabase {
			return instance ?: synchronized(this) {
				instance ?: buildDatabase(context).also { instance = it }
			}
		}

		private fun buildDatabase(context: Context): ServersDatabase {
			return Room.databaseBuilder(context, ServersDatabase::class.java, DATABASE).build()
		}
	}
}