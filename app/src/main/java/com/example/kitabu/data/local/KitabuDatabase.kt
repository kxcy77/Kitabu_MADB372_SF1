package com.example.kitabu.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.TimeUnit

@Database(
    entities = [BookEntity::class, BookingEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
abstract class KitabuDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val books = listOf(
                arrayOf<Any>(1L, "Clean Code", "Robert C. Martin", "Software Engineering", 0),
                arrayOf<Any>(2L, "Introduction to Algorithms", "Thomas H. Cormen", "Computer Science", 0),
                arrayOf<Any>(3L, "Kotlin in Action", "Dmitry Jemerov", "Programming", 1),
                arrayOf<Any>(4L, "Database System Concepts", "Abraham Silberschatz", "Databases", 1),
                arrayOf<Any>(5L, "Designing Data-Intensive Applications", "Martin Kleppmann", "Data Engineering", 1),
                arrayOf<Any>(6L, "The Pragmatic Programmer", "David Thomas", "Software Engineering", 1),
                arrayOf<Any>(7L, "Android Programming", "Bill Phillips", "Mobile Development", 1),
                arrayOf<Any>(8L, "Computer Networks", "Andrew S. Tanenbaum", "Networking", 1),
                arrayOf<Any>(9L, "Artificial Intelligence: A Modern Approach", "Stuart Russell", "Artificial Intelligence", 1),
                arrayOf<Any>(10L, "Operating System Concepts", "Abraham Silberschatz", "Operating Systems", 1),
                arrayOf<Any>(11L, "Discrete Mathematics and Its Applications", "Kenneth Rosen", "Mathematics", 1),
                arrayOf<Any>(12L, "Research Methodology", "Ranjit Kumar", "Research", 1),
            )
            books.forEach { values ->
                db.execSQL(
                    "INSERT INTO books (book_id, title, author, category, is_available) VALUES (?, ?, ?, ?, ?)",
                    values,
                )
            }

            val now = System.currentTimeMillis()
            db.execSQL(
                "INSERT INTO bookings (booking_id, book_owner_id, user_name, booking_date, return_deadline, status) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(1L, 1L, "Demo Student", now, now + TimeUnit.DAYS.toMillis(14), "PENDING"),
            )
            db.execSQL(
                "INSERT INTO bookings (booking_id, book_owner_id, user_name, booking_date, return_deadline, status) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(2L, 2L, "Demo Student", now - TimeUnit.DAYS.toMillis(4), now + TimeUnit.DAYS.toMillis(10), "ACTIVE"),
            )
        }
    }

    companion object {
        @Volatile
        private var instance: KitabuDatabase? = null

        fun getInstance(context: Context): KitabuDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KitabuDatabase::class.java,
                    "kitabu.db",
                )
                    .addCallback(SeedCallback())
                    .build()
                    .also { instance = it }
            }
    }
}
