package org.beobma.classwar.classlist.quest

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

data class Quest(
    val name: String,
    val description: String,
    val objectives: List<QuestObjective>,
    val rewards: List<QuestReward>,
    val player: Player,
    var isCompleted: Boolean = false
) {
    fun completeQuest() {
        if (objectives.all { it.isCompleted }) {
            rewards.forEach { it.giveReward(player) }
            isCompleted = true
        }
    }
}

data class QuestObjective(
    val description: String,
    var isCompleted: Boolean = false
)

sealed class QuestReward(val rewardType: RewardType) {
    abstract fun giveReward(player: Player)

    class ItemReward(val itemStack: ItemStack, val amount: Int) : QuestReward(RewardType.ITEM) {
        override fun giveReward(player: Player) {
            val inventory = player.inventory
            val cloneItemStack = itemStack.clone()
            cloneItemStack.amount = this.amount
            if (inventory.firstEmpty() != -1) {
                inventory.addItem(cloneItemStack)
            } else {
                player.world.dropItemNaturally(player.location, cloneItemStack)
            }
        }
    }

    class ExperienceReward(val amount: Int) : QuestReward(RewardType.EXPERIENCE) {
        override fun giveReward(player: Player) {
            player.giveExp(amount)
        }
    }
}

enum class RewardType {
    ITEM, EXPERIENCE
}


object QuestManager {

    fun createQuest(
        name: String,
        description: String,
        objectiveDescriptions: List<String>,
        rewardTypes: List<RewardType>,
        rewardAmounts: List<Int>,
        rewardItems: List<ItemStack?>,
        player: Player
    ): Quest {
        val objectives = objectiveDescriptions.map { QuestObjective(it) }
        val rewards = rewardTypes.zip(rewardAmounts.zip(rewardItems)).map { (type, pair) ->
            val (amount, item) = pair
            when (type) {
                RewardType.ITEM -> item?.let { QuestReward.ItemReward(it, amount) }
                    ?: throw IllegalArgumentException("ItemStack must be provided for ITEM reward type")
                RewardType.EXPERIENCE -> QuestReward.ExperienceReward(amount)
            }
        }
        return Quest(name, description, objectives, rewards, player)
    }

    fun createItem(itemMaterial: Material, itemName: String, itemLore: List<String>): ItemStack {
        val item = ItemStack(itemMaterial)
        val meta: ItemMeta = item.itemMeta

        meta.setDisplayName(itemName)
        meta.lore = itemLore

        item.itemMeta = meta
        return item
    }
}