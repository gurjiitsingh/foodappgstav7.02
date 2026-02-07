package com.it10x.foodappgstav7_02.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tables")
data class TableEntity(
    @PrimaryKey val id: String,

    /** Human-readable table name or number, e.g. "Table 1" */
    val tableName: String,

    /** Current table status */
    val status: String,  // AVAILABLE / OCCUPIED / BILL_REQUESTED / CLEANING / RESERVED

    /** Optional assigned waiter or staff member */
    val waiterName: String? = null,
    val waiterId: String? = null,

    /** Firestore ID of the active order linked to this table (if any) */
    val activeOrderId: String? = null,

    /** Number of guests currently seated */
    val guestsCount: Int? = null,

    /** Area where table is located (e.g., "Ground Floor", "Restaurant", etc.) */
    val area: String? = null, // ✅ new field

    /** Sort order within the area */
    val sortOrder: Int? = null, // ✅ new field

    /** When table was last updated (Unix time ms) */
    val updatedAt: Long? = null,

    /** When table was created (Unix time ms) */
    val createdAt: Long? = null,

    /** Optional notes (special requests, reservation name, etc.) */
    val notes: String? = null,

    /** Whether the table is synced with POS device (for offline sync) */
    val synced: Boolean? = null
)
