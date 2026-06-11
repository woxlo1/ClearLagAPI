package com.woxloi.clearlagapi.inventory

import com.woxloi.clearlagapi.ClearLagAPI
import com.woxloi.clearlagapi.constant.ClearLagConstants
import com.woxloi.clearlagapi.manager.DataManager
import org.bukkit.Material
import org.bukkit.entity.Player
import oraserver.orapluginapi.inventory.OraInventoryItem
import oraserver.orapluginapi.inventory.OraPagedInventory
import java.text.SimpleDateFormat
import java.util.*

/**
 * OP専用：全プレイヤーの削除アイテムを閲覧するGUI。
 * targetName を指定すると特定プレイヤーのみ表示。
 * 閲覧のみ（復元操作なし）。
 */
class AdminItemsInventory(
    private val targetName: String? = null
) : OraPagedInventory(
    title = if (targetName != null)
        "§c§f${targetName}§cのアイテム"
    else
        "§c全削除アイテム一覧"
) {

    override fun onBuild(
        player: Player
    ): List<OraInventoryItem> {

        val sdf =
            SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

        val now =
            System.currentTimeMillis()

        return DataManager
            .getAllItems()
            .filter { data ->
                !data.restored &&
                        now - data.clearedAt < ClearLagConstants.ITEM_EXPIRE_TIME &&
                        (targetName == null ||
                                data.ownerName.equals(targetName, ignoreCase = true))
            }
            .sortedByDescending { it.clearedAt }
            .map { data ->

                OraInventoryItem(data.item.clone())
                    .setCanClick(false)
                    .addLore("")
                    .addLore("§7プレイヤー:")
                    .addLore("§f${data.ownerName}")
                    .addLore("")
                    .addLore("§7削除日時:")
                    .addLore("§f${sdf.format(Date(data.clearedAt))}")
                    .addLore("")
                    .addLore("§7残り期限:")
                    .addLore(
                        "§f${formatRemaining(
                            ClearLagConstants.ITEM_EXPIRE_TIME - (now - data.clearedAt)
                        )}"
                    )
                    .addLore("")
                    .addLore("§7※閲覧専用")
            }
    }

    override fun onBarRender(player: Player) {

        val now = System.currentTimeMillis()

        val count = DataManager.getAllItems().count { data ->
            !data.restored &&
                    now - data.clearedAt < ClearLagConstants.ITEM_EXPIRE_TIME &&
                    (targetName == null ||
                            data.ownerName.equals(targetName, ignoreCase = true))
        }

        setItem(
            contentSize + 4,
            OraInventoryItem(Material.CHEST)
                .setDisplayName("§e保存中アイテム")
                .addLore("§7アイテム数: §f$count")
                .addLore(
                    if (targetName != null)
                        "§7対象: §f$targetName"
                    else
                        "§7対象: §f全プレイヤー"
                )
                .setCanClick(false)
        )
    }

    private fun formatRemaining(ms: Long): String {
        if (ms <= 0) return "期限切れ"
        val days = ms / (24 * 60 * 60 * 1000)
        val hours = (ms % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        return if (days > 0) "${days}日${hours}時間" else "${hours}時間"
    }
}
