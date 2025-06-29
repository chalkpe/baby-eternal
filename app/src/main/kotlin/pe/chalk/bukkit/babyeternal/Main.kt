package pe.chalk.bukkit.babyeternal

import java.util.UUID
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Ageable
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class Main : JavaPlugin(), Listener {
    var task: BukkitTask? = null

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        task =
                (object : BukkitRunnable() {
                            override fun run() {
                                getRegisteredEntities().forEach { it.age = -32768 }
                            }
                        })
                        .runTaskTimer(this, 0L, 6000L) // every 5 minutes
    }

    override fun onDisable() {
        task?.cancel()
        task = null
    }

    override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<out String>
    ): Boolean {
        if (sender is Player && command.name == "babies") {
            getRegisteredEntities().forEach {
                it.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 30, 0))
            }
            return true
        }
        return false
    }

    private fun getRegisteredEntities(): List<Ageable> {
        val registrationIds = getRegistrations().map { it.id }
        return server.worlds.flatMap { it.entities.map { it as? Ageable }.filterNotNull() }.filter {
            registrationIds.contains(it.uniqueId.toString())
        }
    }

    private fun getRegistrations(): List<Registration> {
        val list = config.getMapList("registrations")
        return list.map { Registration.fromMap(it) }
    }

    private fun setRegistrations(registrations: List<Registration>) {
        config.set("registrations", registrations.map { it.toMap() })
        saveConfig()
    }

    private fun updateRegistration(id: String, owner: String) {
        val registrations = getRegistrations()
        val registration = Registration(id, owner)
        setRegistrations(
                if (registrations.any { it.id == id }) {
                    registrations.map { if (it.id == id) registration else it }
                } else {
                    registrations + registration
                }
        )
    }

    private fun removeRegistration(id: String) {
        setRegistrations(getRegistrations().filter { it.id != id })
    }

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand
        if (item.type != Material.PUFFERFISH) return

        val animal = event.rightClicked as? Tameable ?: return
        if (player.uniqueId != animal.owner?.uniqueId) return

        val baby = animal as? Ageable ?: return
        val id = baby.uniqueId.toString()
        val owner = player.uniqueId.toString()

        val registration = getRegistrations().find { it.id == id }
        when {
            registration == null -> {
                baby.age = -32768
                updateRegistration(id, owner)
                baby.world.spawnParticle(
                        Particle.HAPPY_VILLAGER,
                        baby.location,
                        10,
                        0.5,
                        0.5,
                        0.5,
                        0.1
                )
                player.sendMessage("아기로 등록되었습니다. 앞으로 성장하지 않습니다.")
            }
            registration.owner == owner -> {
                baby.setAdult()
                removeRegistration(id)
                baby.world.spawnParticle(
                        Particle.HAPPY_VILLAGER,
                        baby.location,
                        10,
                        0.5,
                        0.5,
                        0.5,
                        0.1
                )
                player.sendMessage("등록을 해제했습니다. 아기가 다시 성장합니다.")
            }
            else -> {
                val realOwner = server.getOfflinePlayer(UUID.fromString(registration.owner))
                player.sendMessage("오류: ${realOwner.name}님의 아기로 등록되어 있습니다.")
            }
        }
    }
}
