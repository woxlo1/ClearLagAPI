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

        // 🟡 修正: 起動時にクリーンアップして不要データを即時削除・保存
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

    /**
     * 🟢 修正: save() を呼ばないバージョン。
     * ClearLagListener からまとめて呼び出し、最後に1回だけ save() する。
     */
    fun addItemWithoutSave(
        owner: UUID,
        ownerName: String,
        item: ItemStack
    ) {
        items += ClearedItemData(
            UUID.randomUUID(),
            owner,
            ownerName,
            item,
            System.currentTimeMillis(),
            false
        )
    }

    fun getItems(
        owner: UUID
    ): List<ClearedItemData> {

        return items.filter {
            it.owner == owner
        }
    }

    /**
     * 全プレイヤーのアイテムを返す（OP用）
     */
    fun getAllItems(): List<ClearedItemData> {
        return items.toList()
    }

    /**
     * 🟡 修正: restored フラグを立て、ファイルからも除去して保存。
     * Inventory 側の data.restored 直接変更は廃止し、ここに統一。
     */
    fun markRestored(
        id: UUID
    ) {
        // 🟢 修正: restored済みアイテムをリストから除去してファイル肥大化を防ぐ
        items.removeIf { it.id == id }

        save()
    }

    /**
     * 🟡 修正: 期限切れ・restored済みを両方除去し、変更があった場合のみ保存。
     */
    private fun cleanup() {

        val now =
            System.currentTimeMillis()

        val removed = items.removeIf {
            it.restored ||
                    now - it.clearedAt >
                    ClearLagConstants.ITEM_EXPIRE_TIME
        }

        // 変更があったときだけ保存してI/Oを節約
        if (removed) save()
    }
}
