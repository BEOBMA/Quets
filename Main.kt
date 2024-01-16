package org.beobma.kotrintest

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin(), Listener {

    companion object {
        lateinit var instance: KotrinTest
        lateinit var onlinePlayer: MutableCollection<Player>
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        instance = this
        onlinePlayer = mutableListOf()
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        onlinePlayer.add(player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        onlinePlayer.remove(player)
    }
}
