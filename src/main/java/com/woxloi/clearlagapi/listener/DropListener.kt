package com.woxloi.clearlagapi.listener

import com.woxloi.clearlagapi.ClearLagAPI
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.persistence.PersistentDataType

class DropListener : Listener {

    companion object {

        val OWNER_KEY = NamespacedKey(
            ClearLagAPI.instance,
            "owner"
        )

        val OWNER_NAME_KEY = NamespacedKey(
            ClearLagAPI.instance,
            "owner_name"
        )
    }

    @EventHandler
    fun onDrop(
        event: PlayerDropItemEvent
    ) {

        event.itemDrop
            .persistentDataContainer
            .set(
                OWNER_KEY,
                PersistentDataType.STRING,
                event.player.uniqueId.toString()
            )

        event.itemDrop
            .persistentDataContainer
            .set(
                OWNER_NAME_KEY,
                PersistentDataType.STRING,
                event.player.name
            )
    }
}