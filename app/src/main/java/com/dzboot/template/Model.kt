package com.dzboot.template

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models")
internal class Model {

	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "id")
	private var id = 0
}