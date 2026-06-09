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

class ClearedItemsInventory : OraPagedInventory(
    title = "§c削除されたアイテム一覧"
) {

    override fun onBuild(
        player: Player
    ): List<OraInventoryItem> {

        val sdf =
            SimpleDateFormat(
                "yyyy/MM/dd HH:mm:ss"
            )

        val now =
            System.currentTimeMillis()

        return DataManager
            .getItems(player.uniqueId)
            .filter {

                !it.restored &&
                        now - it.clearedAt <
                        ClearLagConstants.ITEM_EXPIRE_TIME
            }
            .sortedByDescending {
                it.clearedAt
            }
            .map { data ->

                OraInventoryItem(
                    data.item.clone()
                )
                    .setCanClick(false)
                    .addLore("")
                    .addLore(
                        "§7削除日時:"
                    )
                    .addLore(
                        "§f${sdf.format(Date(data.clearedAt))}"
                    )
                    .addLore("")
                    .addLore(
                        "§7落とした主:"
                    )
                    .addLore(
                        "§f${data.ownerName}"
                    )
                    .addLore("")
                    .addLore(
                        "§aクリックで復元"
                    )
                    .setClickEvent { e ->

                        val p =
                            e.whoClicked as Player

                        if (data.restored) {
                            p.sendMessage(
                                ClearLagAPI.prefix +
                                        "§c既に復元済みです"
                            )
                            refresh(p)
                            return@setClickEvent
                        }

                        if (
                            System.currentTimeMillis() -
                            data.clearedAt >=
                            ClearLagConstants.ITEM_EXPIRE_TIME
                        ) {

                            p.sendMessage(
                                ClearLagAPI.prefix +
                                        "§c受け取り期限が切れています"
                            )

                            refresh(p)
                            return@setClickEvent
                        }

                        val remain =
                            p.inventory.addItem(
                                data.item.clone()
                            )

                        if (
                            remain.isNotEmpty()
                        ) {

                            p.sendMessage(
                                ClearLagAPI.prefix +
                                        "§cインベントリに空きがありません"
                            )

                            return@setClickEvent
                        }

                        data.restored = true

                        DataManager.save()

                        p.sendMessage(
                            ClearLagAPI.prefix +
                                    "§aアイテムを復元しました"
                        )

                        refresh(p)
                    }
            }
    }

    override fun onBarRender(
        player: Player
    ) {

        val now =
            System.currentTimeMillis()

        setItem(
            contentSize + 4,
            OraInventoryItem(
                Material.CHEST
            )
                .setDisplayName(
                    "§e取得可能アイテム"
                )
                .addLore(
                    "§7アイテム数: ${
                        DataManager.getItems(
                            player.uniqueId
                        ).count {

                            !it.restored &&
                                    now - it.clearedAt <
                                    ClearLagConstants.ITEM_EXPIRE_TIME
                        }
                    }"
                )
        )
    }
}