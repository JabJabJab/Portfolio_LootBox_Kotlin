package jab.spigot.lootbox

import jab.spigot.util.nms.NMSUtils
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.plugin.java.JavaPlugin

/**
 * JavaPlugin instance for the LootBox plug-in.
 *
 * @author Jab
 */
class Main : JavaPlugin() {

    var cfgManager: CFGManager? = null
        private set
    private var lootBoxManager: LootBoxManager? = null

    override fun onEnable() {
        // Load config.
        saveDefaultConfig()

        // Read config.
        cfgManager = CFGManager(this)
        val cfgManager = this.cfgManager!!
        cfgManager.read()

        lootBoxManager = LootBoxManager(this)
        val lootBoxManager = this.lootBoxManager!!
        lootBoxManager.start()

        // Register the command.
        val lootBoxCommand = LootBoxCommand(this)
        val pCommand = getCommand("lootbox")
            ?: throw RuntimeException(
                "The command 'lootbox' does not exist for the Plugin. Disabling...")
        pCommand.setExecutor(lootBoxCommand)
        pCommand.tabCompleter = lootBoxCommand
    }

    override fun onDisable() {
        if (lootBoxManager != null && lootBoxManager!!.isRunning()) {
            lootBoxManager!!.stop()
            lootBoxManager = null
        }
        cfgManager = null
    }

    companion object {
        /**
         * Lazy method for warning the console for issues with the plug-in.
         *
         * @param message The message to send to the console.
         */
        fun warn(message: String) {
            println(ChatColor.YELLOW.toString() + "WARNING: " + message)
        }

        /**
         * @param inventory The inventory to test.
         * @param slots The amount of slots required to pass.
         * @return Returns true if the player's inventory has at least the amount of slots given that are
         * empty.
         */
        fun hasRoom(inventory: PlayerInventory, slots: Int): Boolean {
            // Start at number -5 to negate the armor slots.
            var openSlots = -5
            for (index in 0 until inventory.size) {
                val next = inventory.getItem(index)
                if (next == null || next.type == Material.AIR) {
                    openSlots++
                }
                if (openSlots == slots) return true
            }
            return false
        }

        @Suppress("DEPRECATION")
        fun createItemHoverText(message: String?, item: ItemStack): TextComponent {
            val itemJson: String = NMSUtils.convertItemStackToJson(item)
            // Prepare a BaseComponent array with the itemJson as a text component.
            val hoverEventComponents = arrayOf<BaseComponent>(
                TextComponent(
                    itemJson) // The only element of the hover events base components is the item json.
            )
            // Create the hover event.
            val event = HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents)
            /*
             * And now we create the text component (this is the actual text that the player sees) and set it's hover
             * event to the item event.
             */
            val component = TextComponent(message)
            component.hoverEvent = event
            return component
        }
    }
}