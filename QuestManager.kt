package org.beobma.kotrintest

import NPCCondition
import Quest
import QuestCompletionCondition
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import java.io.File
import java.io.IOException
import java.util.*

object QuestManager {
    private val folder: File = File(KotrinTest.instance.dataFolder, "/Quests")
    private var questList = mutableListOf<Quest>()

    /**
     * 정의한 퀘스트를 생성하고 등록함
     */
    fun create(quest: Quest) {
        val file = File(folder, "${quest.name}.yml")
        val config = YamlConfiguration.loadConfiguration(file)
        if (file.exists()) {
            return
        }
        config.set("name", quest.name)
        config.set("description", quest.description)
        config.set("reward", serializeItemStackList(quest.reward))
        config.set("npc", quest.npc.uniqueId.toString())
        config.set("questCompletionCondition", quest.completionCondition.getIdentifier())
        config.set("isClear", quest.isClear)
        config.set("isStart", quest.isStart)
        questList.add(quest)

        try {
            config.save(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 이미 등록된 퀘스트를 동일한 이름의 퀘스트로 덮어 씌움
     */
    fun save(quest: Quest) {
        val file = File(folder, "${quest.name}.yml")
        val config = YamlConfiguration.loadConfiguration(file)

        if (!file.exists()) {
            return
        }

        config.set("name", quest.name)
        config.set("description", quest.description)
        config.set("reward", serializeItemStackList(quest.reward))
        config.set("npc", quest.npc.uniqueId.toString())
        config.set("questCompletionCondition", quest.completionCondition.getIdentifier())
        config.set("isClear", quest.isClear)
        config.set("isStart", quest.isStart)
        questList.add(quest)

        try {
            config.save(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 등록된 퀘스트를 불러옴
     */
    fun load() {
        questList.clear()
        val files = folder.listFiles() ?: return

        for (file in files) {
            val config = YamlConfiguration.loadConfiguration(file)

            val name = file.nameWithoutExtension
            val description = config.getStringList("description")
            val rewardList = config.getList("reward")
            val reward = if (rewardList is List<*>) {
                deserializeItemStackList(rewardList.filterIsInstance<Map<String, Any>>())
            } else {
                listOf()
            }
            val npcId = UUID.fromString(config.getString("npc"))
            val npc = Bukkit.getEntity(npcId)
            val conditionIdentifier = config.getString("questCompletionCondition")
            val isClear = config.getBoolean("isClear")
            val isStart = config.getBoolean("isStart")

            if (npc is Entity){
                if (conditionIdentifier is String){
                    val completionCondition = getConditionByIdentifier(conditionIdentifier, npc)
                    val quest = Quest(name, description, reward, npc, completionCondition, isStart, isClear)
                    questList.add(quest)
                }

            }
        }
    }

    private fun getConditionByIdentifier(identifier: String, npc: Entity): QuestCompletionCondition {
        return when (identifier) {
            "SpecificCondition" -> NPCCondition(npc)
            else -> throw IllegalArgumentException("Unknown completion condition identifier: $identifier")
        }
    }

    private fun serializeItemStackList(list: List<ItemStack>): List<Map<String, Any>> {
        return list.map { it.serialize() }
    }

    private fun deserializeItemStackList(list: List<Map<String, Any>>): List<ItemStack> {
        return list.map { ItemStack.deserialize(it) }
    }

    /**
     * 문자열에 해당되는 퀘스트 이름을 가진 퀘스트를 반환
     */
    fun getQuestById(name: String): Quest? {
        return questList.find { it.name == name }
    }

    /**
     * 등록된 퀘스트를 모두 불러옴
     */
    fun getAllQuests(): MutableList<Quest> {
        return questList
    }

    /**
     * 매개변수의 NPC가 등록된 퀘스트중 동일한 NPC가 존재할 때 반환
     */
    fun getNpc(npc: Entity): Quest? {
        return questList.find { it.npc == npc }
    }
}
