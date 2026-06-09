package com.woxloi.clearlagapi.manager

import com.woxloi.clearlagapi.ClearLagAPI
import com.woxloi.clearlagapi.constant.ClearLagConstants
import com.woxloi.clearlagapi.data.ClearedItemData
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.UUID

object DataManager {

    private val items =
        mutableListOf<ClearedItemData>()

    private lateinit var file: File
    private lateinit var yaml: YamlConfiguration

    fun load() {

        file = File(
            ClearLagAPI.instance.dataFolder,
            "data.yml"
        )

        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }

        yaml =
            YamlConfiguration.loadConfiguration(
                file
            )

        items.clear()

        val section =
            yaml.getConfigurationSection(
                "items"
            ) ?: return

        section.getKeys(false)
            .forEach { id ->

                val path = "items.$id"

                val owner =
                    UUID.fromString(
                        yaml.getString(
                            "$path.owner"
                        ) ?: return@forEach
                    )

                val ownerName =
                    yaml.getString(
                        "$path.ownerName"
                    ) ?: "Unknown"

                val item =
                    yaml.getItemStack(
                        "$path.item"
                    ) ?: return@forEach

                val clearedAt =
                    yaml.getLong(
                        "$path.clearedAt"
                    )

                val restored =
                    yaml.getBoolean(
                        "$path.restored"
                    )

                items += ClearedItemData(
                    UUID.fromString(id),
                    owner,
                    ownerName,
                    item,
                    clearedAt,
                    restored
                )
            }

        cleanup()
    }

    fun save() {

        yaml.set("items", null)

        items.forEach {

            val path =
                "items.${it.id}"

            yaml.set(
                "$path.owner",
                it.owner.toString()
            )

            yaml.set(
                "$path.ownerName",
                it.ownerName
            )

            yaml.set(
                "$path.clearedAt",
                it.clearedAt
            )

            yaml.set(
                "$path.restored",
                it.restored
            )

            yaml.set(
                "$path.item",
                it.item
            )
        }

        yaml.save(file)
    }

    fun addItem(
        owner: UUID,
        ownerName: String,
        item: ItemStack
    ) {

        cleanup()

        items += ClearedItemData(
            UUID.randomUUID(),
            owner,
            ownerName,
            item,
            System.currentTimeMillis(),
            false
        )

        save()
    }

    fun getItems(
        owner: UUID
    ): List<ClearedItemData> {

        cleanup()

        return items.filter {
            it.owner == owner
        }
    }

    fun markRestored(
        id: UUID
    ) {

        items.firstOrNull {
            it.id == id
        }?.restored = true

        save()
    }

    private fun cleanup() {

        val now =
            System.currentTimeMillis()

        items.removeIf {
            now - it.clearedAt >
                    ClearLagConstants.ITEM_EXPIRE_TIME
        }
    }
}