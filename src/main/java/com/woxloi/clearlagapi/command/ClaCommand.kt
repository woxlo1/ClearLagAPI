package com.woxloi.clearlagapi.command

import com.woxloi.clearlagapi.ClearLagAPI
import com.woxloi.clearlagapi.inventory.AdminItemsInventory
import com.woxloi.clearlagapi.inventory.ClearedItemsInventory
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ClaCommand : CommandExecutor, TabCompleter {

    // 一般プレイヤー向けサブコマンド
    private val playerSubCommands = listOf("list", "help")

    // OP専用サブコマンド
    private val opSubCommands = listOf("op", "help")

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        if (sender !is Player)
            return true

        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0].lowercase()) {

            "list" -> {
                ClearedItemsInventory().open(sender)
            }

            "op" -> {
                if (!sender.isOp) {
                    sender.sendMessage(
                        ClearLagAPI.prefix + "§c権限がありません"
                    )
                    return true
                }

                // /cla admin [プレイヤー名]
                val targetName = args.getOrNull(1)

                if (targetName != null) {
                    // 存在するプレイヤー名かチェック（オフライン含む）
                    val known = Bukkit.getOfflinePlayers()
                        .any { it.name.equals(targetName, ignoreCase = true) }

                    if (!known) {
                        sender.sendMessage(
                            ClearLagAPI.prefix + "§c§f$targetName§cが見つかりません"
                        )
                        return true
                    }

                    AdminItemsInventory(targetName).open(sender)
                } else {
                    AdminItemsInventory().open(sender)
                }
            }

            "help" -> {
                sendHelp(sender)
            }

            else -> {
                sender.sendMessage(
                    ClearLagAPI.prefix + "§c不明なサブコマンドです。§f/cla help §cで確認してください"
                )
            }
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {

        if (sender !is Player) return emptyList()

        return when (args.size) {

            // 第1引数: サブコマンド補完
            1 -> {
                val subs = if (sender.isOp)
                    (playerSubCommands + opSubCommands).distinct()
                else
                    playerSubCommands

                subs.filter {
                    it.startsWith(args[0].lowercase())
                }
            }

            // 第2引数: admin の場合はオンラインプレイヤー名を補完
            2 -> {
                if (args[0].lowercase() == "op" && sender.isOp) {
                    Bukkit.getOnlinePlayers()
                        .map { it.name }
                        .filter { it.startsWith(args[1], ignoreCase = true) }
                } else {
                    emptyList()
                }
            }

            else -> emptyList()
        }
    }

    private fun sendHelp(player: Player) {
        val p = ClearLagAPI.prefix
        player.sendMessage("$p§e--- ClearLagAPI ヘルプ ---")
        player.sendMessage("§7/cla list §f- 削除されたアイテムを確認・復元する")
        player.sendMessage("§7/cla help  §f- このヘルプを表示する")
        if (player.isOp) {
            player.sendMessage("§7/cla op            §f- 全プレイヤーの削除アイテムを閲覧")
            player.sendMessage("§7/cla op §e<プレイヤー> §f- 特定プレイヤーの削除アイテムを閲覧")
        }
    }
}
