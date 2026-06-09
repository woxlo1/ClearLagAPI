package com.woxloi.clearlagapi.listener

import com.woxloi.clearlagapi.manager.DataManager
import me.minebuilders.clearlag.events.EntityRemoveEvent
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class ClearLagListener : Listener {

    @EventHandler
    fun onClearLag(
        event: EntityRemoveEvent
    ) {

        event.entityList.forEach { entity ->

            if (entity !is Item)
                return@forEach

            val ownerString =
                entity.persistentDataContainer.get(
                    DropListener.OWNER_KEY,
                    PersistentDataType.STRING
                ) ?: return@forEach

            val ownerName =
                entity.persistentDataContainer.get(
                    DropListener.OWNER_NAME_KEY,
                    PersistentDataType.STRING
                ) ?: "Unknown"

            val owner =
                UUID.fromString(
                    ownerString
                )

            DataManager.addItem(
                owner,
                ownerName,
                entity.itemStack.clone()
            )
        }
    }
}