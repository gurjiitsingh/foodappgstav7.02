package com.it10x.foodappgstav7_02.data.pos.repository

import android.util.Log
import com.it10x.foodappgstav7_02.data.pos.dao.KotBatchDao
import com.it10x.foodappgstav7_02.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_02.data.pos.dao.TableDao
import com.it10x.foodappgstav7_02.data.pos.entities.PosKotItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class KotRepository(
    private val batchDao: KotBatchDao,
    private val kotItemDao: KotItemDao,
    private val tableDao: TableDao
) {

    fun getRunningKotsForTable(tableNo: String): Flow<Pair<List<Any>, List<Any>>> {
        return combine(
            batchDao.getBatchesForTable(tableNo),
            kotItemDao.getItemsForTable(tableNo)
        ) { batches, items ->
            batches to items
        }
    }


    suspend fun insertItemsAndSync(
        tableNo: String,
        items: List<PosKotItemEntity>
    ) {
        kotItemDao.insertAll(items)
        syncKitchenCount(tableNo)
    }



    suspend fun markDoneAll(tableNo: String) {
        kotItemDao.markAllDone(tableNo)
        kotItemDao.markAllPrinted(tableNo)

       }

    private suspend fun syncBillCounters(tableNo: String) {
        val billCount = kotItemDao.countDoneItems(tableNo) ?: 0
        val billAmount = kotItemDao.sumDoneAmount(tableNo) ?: 0.0

        tableDao.updateBill(tableNo, billCount, billAmount)
    }

    private suspend fun syncKitchenCount(tableNo: String) {
      //  Log.d("TABLE_DEBUG", "syncKitchenCount() called for table = $tableNo")

        val count = kotItemDao.countKitchenPending(tableNo) ?: 0

      //  Log.d("TABLE_DEBUG", "Kitchen pending count from DB = $count")

        tableDao.setKitchenCount(tableNo, count)

      //  Log.d("TABLE_DEBUG", "Kitchen count updated in tableDao")
    }

    suspend fun syncBillCount(tableNo: String) {
        // 🔥 refresh counters after state change
        syncBillCounters(tableNo)
    }

    suspend fun syncKinchenCount(tableNo: String) {
        // 🔥 refresh counters after state change
        syncKitchenCount(tableNo)
    }

}
