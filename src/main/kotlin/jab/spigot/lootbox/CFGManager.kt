@file:Suppress("unused")

package jab.spigot.lootbox

import jab.spigot.lootbox.Main.Companion.warn
import jab.spigot.util.ColorUtils.Companion.color
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.yaml.snakeyaml.error.YAMLException
import java.text.DecimalFormat

class CFGManager(val plugin: Main) {

    val items: HashMap<String, CFGItem> = HashMap()
    val lootBoxes: HashMap<String, LootBox> = HashMap()

    private var lootboxTableLine: String? = null
    private var itemRarityColor1: String? = null
    private var itemRarityColor5: String? = null
    private var itemRarityColor20: String? = null
    private var itemRarityColor50: String? = null
    private var itemRarityColor100: String? = null
    private var commandGiveHelp: String? = null
    private var commandGiveCommander: String? = null
    private var commandGiveRecipient: String? = null
    private var commandGiveInvalidAmount: String? = null
    private var commandGiveInventoryFullCommander: String? = null
    private var commandGiveInventoryFullRecipient: String? = null
    private var unknownPlayer: String? = null
    private var unknownLootbox: String? = null
    private var unknownItem: String? = null
    private var inventoryFullUse: String? = null

    fun read() {
        val cfg = plugin.config

        // Read the dialogs.
        val dialog = cfg.getConfigurationSection("dialog")!!
        unknownPlayer = color(dialog.getString("unknown_player")!!)
        unknownLootbox = color(dialog.getString("unknown_lootbox")!!)
        unknownItem = color(dialog.getString("unknown_item")!!)
        inventoryFullUse = color(dialog.getString("inventory_full_use")!!)
        commandGiveInvalidAmount = color(dialog.getString("command_give_invalid_amount")!!)
        commandGiveCommander = color(dialog.getString("command_give_commander")!!)
        commandGiveRecipient = color(dialog.getString("command_give_recipient")!!)
        commandGiveInventoryFullCommander = color(dialog.getString("command_give_inventory_full_commander")!!)
        commandGiveInventoryFullRecipient = color(dialog.getString("command_give_inventory_full_recipient")!!)
        commandGiveHelp = color(dialog.getString("command_give_help")!!)
        lootboxTableLine = color(dialog.getString("lootbox_table_line")!!)
        itemRarityColor1 = color(dialog.getString("item_rarity_color_1")!!)
        itemRarityColor5 = color(dialog.getString("item_rarity_color_5")!!)
        itemRarityColor20 = color(dialog.getString("item_rarity_color_20")!!)
        itemRarityColor50 = color(dialog.getString("item_rarity_color_50")!!)
        itemRarityColor100 = color(dialog.getString("item_rarity_color_100")!!)

        // Read the items first.
        val itemsCfg = cfg.getConfigurationSection("items")!!
        for (key in itemsCfg.getKeys(false)) {
            if (!itemsCfg.isConfigurationSection(key)) {
                warn("The lootbox item \"$key\" is not a valid configured section. Ignoring..")
                continue
            }
            try {
                val item = CFGItem(null)
                item.read(itemsCfg.getConfigurationSection(key)!!)
                items[formatId(item.id!!)] = item
            } catch (e: YAMLException) {
                warn("Failed to load lootbox item: $key")
                e.printStackTrace(System.out)
            }
        }

        // Read the lootboxes next, pairing them with the items for the weight table.
        val lootboxes = cfg.getConfigurationSection("lootboxes")!!
        for (key in lootboxes.getKeys(false)) {
            if (!lootboxes.isConfigurationSection(key)) {
                warn("The lootbox \"$key\" is not a valid configured section. Ignoring..")
                continue
            }
            try {
                val lootBox = LootBox(this, lootboxes.getConfigurationSection(key)!!)
                lootBox.pair()
                lootBoxes[formatId(lootBox.id)] = lootBox
            } catch (e: YAMLException) {
                warn("Failed to load lootbox: $key")
                e.printStackTrace(System.out)
            }
        }
    }

    /**
     * Processes a item for the lore of a [lootbox][LootBox].
     *
     * @param item The item to format.
     * @param chance The chance calculated from the WeightTable for the LootBox.
     * @return Returns the formatted table line for the LootBox item's lore.
     */
    fun processTableLine(item: CFGItem, chance: Double): String {
        var fChance = ChatColor.BOLD.toString() + "%" + format.format(chance)
        fChance = when {
            chance <= 2 -> {
                "$itemRarityColor1$fChance"
            }
            chance <= 5 -> {
                "$itemRarityColor5$fChance"
            }
            chance <= 20 -> {
                "$itemRarityColor20$fChance"
            }
            chance <= 50 -> {
                "$itemRarityColor50$fChance"
            }
            else -> {
                "$itemRarityColor100$fChance"
            }
        }
        return getLootboxTableLine(item.displayName, fChance)
    }

    /**
     * @param id The ID of the lootbox.
     * @return Returns the lootbox with the ID given. If no lootbox ID matches the ID given, NULL is
     * returned.
     */
    fun getLootBox(id: String): LootBox? {
        return lootBoxes[formatId(id)]
    }


    /**
     * @param id The ID of the item.
     * @return Returns the item with the ID given. If no item ID matches the ID given, NULL is
     * returned.
     */
    fun getItem(id: String): CFGItem? {
        return items[formatId(id)]
    }

    /**
     * @return Returns all loaded lootboxes.
     */
    fun getLootBoxes(): Collection<LootBox?> {
        return lootBoxes.values
    }

    /**
     * @return Returns all loaded items.
     */
    fun getItems(): Collection<CFGItem?> {
        return items.values
    }

    fun getCommandGiveCommander(player: Player, item: String, amount: Int): String {
        return commandGiveCommander!!
            .replace("%player%".toRegex(), player.name)
            .replace("%item%".toRegex(), item)
            .replace("%amount%".toRegex(), "" + amount)
    }

    fun getInventoryFullUse(item: String?, minSlots: Int): String {
        return inventoryFullUse!!.replace("%min_slots%".toRegex(), "" + minSlots).replace("%item%".toRegex(), item!!)
    }

    fun getCommandGiveRecipiant(item: String, amount: Int): String {
        return commandGiveRecipient!!.replace("%item%".toRegex(), item).replace("%amount%".toRegex(), "" + amount)
    }

    fun getCommandGiveInventoryFullCommander(
        player: Player, item: String, amount: Int,
    ): String {
        return commandGiveInventoryFullCommander!!
            .replace("%player%".toRegex(), player.name)
            .replace("%item%".toRegex(), item)
            .replace("%amount%".toRegex(), "" + amount)
    }

    fun getCommandGiveInventoryFullRecipient(
        item: String, amount: Int, minSlots: Int,
    ): String {
        return commandGiveInventoryFullRecipient!!
            .replace("%item%".toRegex(), item)
            .replace("%amount%".toRegex(), "" + amount)
            .replace("%min_slots%".toRegex(), "" + minSlots)
    }

    fun getLootboxTableLine(item: String?, chance: String): String {
        var result = lootboxTableLine!!
        if (item != null) {
            result = result.replace("%item%".toRegex(), item)
        }
        return result.replace("%chance%".toRegex(), chance)
    }

    fun getCommandGiveInvalidAmount(amount: String?): String {
        return commandGiveInvalidAmount!!.replace("%amount%".toRegex(), amount!!)
    }

    fun getItemRarityColor1(): String? {
        return itemRarityColor1
    }

    fun getItemRarityColor5(): String? {
        return itemRarityColor5
    }

    fun getItemRarityColor20(): String? {
        return itemRarityColor20
    }

    fun getItemRarityColor50(): String? {
        return itemRarityColor50
    }

    fun getItemRarityColor100(): String? {
        return itemRarityColor100
    }

    fun getCommandGiveHelp(): String? {
        return commandGiveHelp
    }

    fun getUnknownItem(item: String): String {
        return unknownItem!!.replace("%item%".toRegex(), item)
    }

    fun getUnknownLootbox(lootBox: String): String {
        return unknownLootbox!!.replace("%lootbox%".toRegex(), lootBox)
    }

    fun getUnknownPlayer(player: String?): String {
        return unknownPlayer!!.replace("%player%".toRegex(), player!!)
    }

    companion object {

        val format: DecimalFormat = DecimalFormat("00.00")

        /**
         * @param id The ID to format.
         * @return Returns the properly formatted ID to use to store and access internal maps.
         */
        fun formatId(id: String): String {
            return id.toLowerCase()
        }
    }
}