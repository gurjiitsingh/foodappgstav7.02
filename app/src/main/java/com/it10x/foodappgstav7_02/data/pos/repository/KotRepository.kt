package com.it10x.foodappgstav7_02.data.pos.repository

import com.it10x.foodappgstav7_02.data.pos.dao.KotBatchDao
import com.it10x.foodappgstav7_02.data.pos.dao.KotItemDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class KotRepository(
    private val batchDao: KotBatchDao,
    private val itemDao: KotItemDao
) {

    fun getRunningKotsForTable(tableNo: String): Flow<Pair<List<Any>, List<Any>>> {
        return combine(
            batchDao.getBatchesForTable(tableNo),
            itemDao.getItemsForTable(tableNo)
        ) { batches, items ->
            batches to items
        }
    }
}
