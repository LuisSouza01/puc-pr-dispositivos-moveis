package br.pucpr.hortifrutidobairro.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// DAO (Data Access Object): the operations we can run on the "products" table.
// Room generates the implementation of this interface at compile time.
@Dao
interface ProductDao {

    // Write operations are "suspend" so they run inside a coroutine,
    // off the main thread.
    @Insert
    suspend fun insert(product: Product)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    // Flow: emits a new list every time the table changes,
    // so the screen updates automatically.
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun listAll(): Flow<List<Product>>

    // Only the products marked as available for donation.
    @Query("SELECT * FROM products WHERE availableForDonation = 1 ORDER BY name ASC")
    fun listDonations(): Flow<List<Product>>
}
