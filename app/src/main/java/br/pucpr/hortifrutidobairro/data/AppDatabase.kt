package br.pucpr.hortifrutidobairro.data

import androidx.room.Database
import androidx.room.RoomDatabase

// Database: lists the entities (tables) and the schema version.
// Room generates the concrete class that creates the SQLite file.
@Database(entities = [Product::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    // Gives access to the DAO of the products table.
    abstract fun productDao(): ProductDao
}
