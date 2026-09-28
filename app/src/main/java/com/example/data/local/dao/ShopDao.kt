package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ShopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {

    @Query("SELECT * FROM shops ORDER BY rating DESC, id DESC")
    fun getAllShops(): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops WHERE category = :category ORDER BY rating DESC")
    fun getShopsByCategory(category: String): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops WHERE id = :id LIMIT 1")
    fun getShopById(id: Long): Flow<ShopEntity?>

    @Query("SELECT * FROM shops WHERE shopName = :name LIMIT 1")
    fun getShopByName(name: String): Flow<ShopEntity?>

    @Query("SELECT * FROM shops WHERE ownerMobile = :mobile LIMIT 1")
    fun getShopByOwnerMobile(mobile: String): Flow<ShopEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShops(shops: List<ShopEntity>)

    @Update
    suspend fun updateShop(shop: ShopEntity)

    @Query("DELETE FROM shops WHERE id = :id")
    suspend fun deleteShopById(id: Long)

    @Query("SELECT COUNT(*) FROM shops")
    fun getShopsCount(): Flow<Int>
}
