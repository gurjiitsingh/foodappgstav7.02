package com.it10x.foodappgstav7_02.data.pos.dao



import androidx.room.Dao
import androidx.room.Query
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderMasterEntity
import kotlinx.coroutines.flow.Flow



@Dao
interface SalesMasterDao {

    // ======================
    // SALES LIST
    // ======================
    @Query("""
    SELECT * FROM pos_order_master
    WHERE orderStatus = 'PAID'
      AND paymentStatus = 'PAID'
      AND createdAt BETWEEN :from AND :to
    ORDER BY createdAt DESC
    """)
    fun getSales(
        from: Long,
        to: Long
    ): Flow<List<PosOrderMasterEntity>>


    // ======================
    // TOTAL SALES
    // ======================
    @Query("""
    SELECT IFNULL(SUM(grandTotal),0)
    FROM pos_order_master
    WHERE orderStatus = 'PAID'
      AND paymentStatus = 'PAID'
      AND createdAt BETWEEN :from AND :to
    """)
    suspend fun getTotalSales(
        from: Long,
        to: Long
    ): Double


    // ======================
    // PAYMENT BREAKUP
    // ======================
    @Query("""
    SELECT paymentType, IFNULL(SUM(grandTotal),0) as total
    FROM pos_order_master
    WHERE orderStatus = 'PAID'
      AND paymentStatus = 'PAID'
      AND createdAt BETWEEN :from AND :to
    GROUP BY paymentType
    """)
    suspend fun getPaymentBreakup(
        from: Long,
        to: Long
    ): List<PaymentBreakup>



    @Query("""
    SELECT * FROM pos_order_master
    WHERE orderStatus = 'COMPLETED'
      AND paymentStatus = 'PAID'
    ORDER BY createdAt DESC
""")
    fun getAllPaidOrders(): Flow<List<PosOrderMasterEntity>>

    // ======================
    // TAX / DISCOUNT
    // ======================
    @Query("""
    SELECT 
        IFNULL(SUM(taxTotal),0) as taxTotal,
        IFNULL(SUM(discountTotal),0) as discountTotal
    FROM pos_order_master
    WHERE orderStatus = 'PAID'
      AND paymentStatus = 'PAID'
      AND createdAt BETWEEN :from AND :to
    """)
    suspend fun getTaxDiscountSummary(
        from: Long,
        to: Long
    ): TaxDiscountSummary
}


data class PaymentBreakup(
    val paymentType: String,
    val total: Double
)

data class TaxDiscountSummary(
    val taxTotal: Double,
    val discountTotal: Double
)
