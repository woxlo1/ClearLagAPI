package com.woxloi.clearlagapi.data

import org.bukkit.inventory.ItemStack
import java.util.UUID

data class ClearedItemData(
    val id: UUID,
    val owner: UUID,
    val ownerName: String,
    val item: ItemStack,
    val clearedAt: Long,
    var restored: Boolean
)
