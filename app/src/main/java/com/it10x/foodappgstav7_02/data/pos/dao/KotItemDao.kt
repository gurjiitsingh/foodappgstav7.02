package com.it10x.foodappgstav7_02.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_02.data.pos.entities.PosKotItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KotItemDao {


    // -------------------------
    // INSERT
    // -------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PosKotItemEntity>)


    // -------------------------
    // INSERT SINGLE ITEM (NEW)
    // -------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PosKotItemEntity)
    // -------------------------
    // FETCH (LIVE BILL)
    // -------------------------
    @Query("""
        SELECT * FROM pos_kot_items
        WHERE tableNo = :tableNo
        ORDER BY createdAt ASC
    """)
    fun getItemsForTable(tableNo: String): Flow<List<PosKotItemEntity>>

    // -------------------------
    // FETCH (FINAL BILL)
    // -------------------------
    @Query("""
        SELECT * FROM pos_kot_items
        WHERE tableNo = :tableNo
    """)
    suspend fun getItemsForTableSync(tableNo: String?): List<PosKotItemEntity>

    // -------------------------
    // CLEANUP AFTER PAYMENT
    // -------------------------
    @Query("""
        DELETE FROM pos_kot_items
        WHERE tableNo = :tableNo
    """)
    suspend fun clearForTable(tableNo: String)


    @Query("""
    UPDATE pos_kot_items
    SET status = :status
    WHERE id = :itemId
""")
    suspend fun updateStatus(
        itemId: String,
        status: String
    )


    @Query("SELECT * FROM pos_kot_items WHERE id = :itemId LIMIT 1")
    suspend fun getItemByIdSync(itemId: String): PosKotItemEntity?
    @Query("""
    SELECT * FROM pos_kot_items
    WHERE tableNo = :tableNo
      AND status = 'PENDING'
    ORDER BY createdAt ASC
""")
    fun getPendingItemsForTable(tableNo: String): Flow<List<PosKotItemEntity>>

    @Query("""
    SELECT * FROM pos_kot_items
    WHERE tableNo = :tableNo
      AND status = 'DONE'
    ORDER BY createdAt ASC
""")
    fun getDoneItemsForTable(tableNo: String): Flow<List<PosKotItemEntity>>

    @Query("DELETE FROM pos_kot_items")
    suspend fun deleteAllKotItems()


    @Query("SELECT * FROM pos_kot_items")
    fun getTotalKotItems(): Flow<List<PosKotItemEntity>>



    @Query("""
SELECT * FROM pos_kot_items
WHERE kotBatchId = :sessionKey
AND status = 'PENDING'
""")
    fun getPendingItemsForSession(sessionKey: String): Flow<List<PosKotItemEntity>>

    @Query("SELECT * FROM pos_kot_items ORDER BY createdAt ASC")
    fun getAllKotItems(): Flow<List<PosKotItemEntity>>

    @Query("""
    UPDATE pos_kot_items
    SET quantity = :quantity
    WHERE id = :itemId
""")
    suspend fun updateQuantity(itemId: String, quantity: Int)

    // -------------------------
// PRINT FLAG
// -------------------------
//    @Query("""
//    UPDATE pos_kot_items
//    SET isPrinted = 1
//    WHERE id = :itemId
//""")
//    suspend fun markPrinted(itemId: String)



    // -------------------------
// FETCH UNPRINTED ITEMS
// -------------------------
    @Query("""
    SELECT * FROM pos_kot_items
    WHERE tableNo = :tableNo
      AND status = 'PENDING'
      AND isPrinted = 0
    ORDER BY createdAt ASC
""")
    suspend fun getUnprintedPendingItems(tableNo: String): List<PosKotItemEntity>

    @Query("""
    SELECT * FROM pos_kot_items
    WHERE tableNo = :tableNo
      AND status = 'PENDING'
      ORDER BY createdAt ASC
""")
    suspend fun getPendingItems(tableNo: String): List<PosKotItemEntity>

    @Query("""
    UPDATE pos_kot_items
    SET isPrinted = 1
    WHERE tableNo = :tableNo
      AND isPrinted = 0
""")
    suspend fun markAllPrintedForTable(tableNo: String)

    @Query("""
    UPDATE pos_kot_items
    SET isPrinted = 1
    WHERE id = :itemId
""")
    suspend fun markPrinted(itemId: String)

    @Query("""
    UPDATE pos_kot_items
    SET isPrinted = 1
    WHERE id IN (:itemIds)
""")
    suspend fun markPrintedBatch(itemIds: List<String>)

    @Query("""
    UPDATE pos_kot_items
    SET status = 'DONE'
    WHERE tableNo = :tableNo
      AND status = 'PENDING'
""")
    suspend fun markAllDone(tableNo: String)

    @Query("""
    UPDATE pos_kot_items
    SET isPrinted = 1
    WHERE tableNo = :tableNo
      AND isPrinted = 0
""")
    suspend fun markAllPrinted(tableNo: String)

    @Query("""
    SELECT * FROM pos_kot_items
    WHERE tableNo = :tableNo
      AND isPrinted = 0
    ORDER BY createdAt ASC
""")
    suspend fun getUnprintedItems(tableNo: String): List<PosKotItemEntity>


    @Query("""
    SELECT COUNT(*) 
    FROM pos_kot_items 
    WHERE sessionId = :sessionId
""")
    suspend fun cartItemCount(sessionId: String): Int


    @Query("""
    SELECT EXISTS(
        SELECT 1 FROM pos_kot_items WHERE sessionId = :sessionId
    )
""")
    suspend fun hasCartItems(sessionId: String): Boolean



    // -------------------------
// TABLE GRID – COUNTS
// -------------------------

    @Query("""
    SELECT COUNT(*) 
    FROM pos_kot_items
    WHERE tableNo = :tableNo
      AND status = 'PENDING'
""")
    suspend fun countKitchenPending(tableNo: String): Int


    @Query("""
    SELECT COUNT(*) 
    FROM pos_kot_items
    WHERE tableNo = :tableNo
      AND status = 'DONE'
""")
    suspend fun countBillDone(tableNo: String): Int






    @Query("SELECT COUNT(*) FROM pos_kot_items WHERE tableNo = :tableNo AND status = 'DONE'")
    suspend fun countDoneItems(tableNo: String): Int?

    @Query("""
SELECT SUM(basePrice * quantity)
FROM pos_kot_items
WHERE tableNo = :tableNo AND status = 'DONE'
""")
    suspend fun sumDoneAmount(tableNo: String): Double?


    // -------------------------
// TABLE GRID – BILL AMOUNT
// -------------------------
    @Query("""
    SELECT 
        IFNULL(SUM(
            (basePrice * quantity) +
            CASE 
                WHEN taxType = 'exclusive'
                THEN (basePrice * quantity * taxRate / 100)
                ELSE 0
            END
        ), 0)
    FROM pos_kot_items
    WHERE tableNo = :tableNo
      AND status = 'DONE'
""")
    suspend fun billAmountForTable(tableNo: String): Double


    @Query("""
    SELECT IFNULL(SUM(
        (basePrice * quantity) +
        CASE 
            WHEN taxType = 'exclusive' 
            THEN (basePrice * quantity * taxRate / 100)
            ELSE 0
        END
    ), 0)
    FROM pos_kot_items
    WHERE tableNo = :tableNo
      AND status IN ('PENDING', 'DONE')
""")
    suspend fun billAmountForTableIncludingKitchen(tableNo: String): Double




}
