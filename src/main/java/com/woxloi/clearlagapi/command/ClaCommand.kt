package com.woxloi.clearlagapi.command

import com.woxloi.clearlagapi.ClearLagAPI
import com.woxloi.clearlagapi.inventory.ClearedItemsInventory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ClaCommand : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        if (sender !is Player)
            return true

        if (args.isEmpty()) {
            sender.sendMessage(ClearLagAPI.prefix + "§cサブコマンドを指定してください")
            return true
        }

        when (args[0].lowercase()) {

            "items" -> {
                ClearedItemsInventory().open(sender)
            }

            else -> {
                sender.sendMessage(ClearLagAPI.prefix + "§c不明なサブコマンドです。")
            }
        }

        return true
    }
}