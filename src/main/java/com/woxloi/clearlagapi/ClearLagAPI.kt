package com.woxloi.clearlagapi

import com.woxloi.clearlagapi.command.ClaCommand
import com.woxloi.clearlagapi.listener.ClearLagListener
import com.woxloi.clearlagapi.listener.DropListener
import com.woxloi.clearlagapi.manager.DataManager
import org.bukkit.plugin.java.JavaPlugin

class ClearLagAPI : JavaPlugin() {

    companion object {
        val prefix = "§8[§6§lClearLagAPI§8] §r"
        lateinit var instance: ClearLagAPI
            private set
    }

    override fun onEnable() {
        instance = this

        saveDefaultConfig()

        DataManager.load()

        server.pluginManager.registerEvents(
            DropListener(),
            this
        )

        server.pluginManager.registerEvents(
            ClearLagListener(),
            this
        )

        getCommand("cla")?.setExecutor(
            ClaCommand()
        )

        logger.info("ClearLagAPI enabled")
    }

    override fun onDisable() {
        DataManager.save()
    }
}
