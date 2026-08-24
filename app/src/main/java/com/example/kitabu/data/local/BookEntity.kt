package com.example.kitabu.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "book_id")
    val bookId: Long = 0,
    val title: String,
    val author: String,
    val category: String,
    @ColumnInfo(name = "is_available", defaultValue = "1")
    val isAvailable: Boolean = true,
)
