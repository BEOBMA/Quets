import org.beobma.kotrintest.KotrinTest.Companion.onlinePlayer
import org.beobma.kotrintest.QuestManager
import org.beobma.kotrintest.QuestManager.save
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack

/**
 * 퀘스트 완료 조건을 정의하는 인터페이스.
 * 퀘스트가 완료되었는지 여부를 판단하는 데 사용되는 메소드를 포함합니다.
 */
interface QuestCompletionCondition {
    /**
     * 퀘스트 완료 여부를 반환합니다.
     * @return 퀘스트가 완료되었으면 true, 그렇지 않으면 false를 반환합니다.
     */
    fun isCompleted(): Boolean
    /**
     * 퀘스트 완료 조건의 고유 식별자를 반환합니다.
     * @return 조건의 고유 식별자를 문자열로 반환합니다.
     */
    fun getIdentifier(): String
}
/**
 * 퀘스트의 생명주기를 관리하는 인터페이스.
 * 퀘스트의 시작, 완료, 보상 지급 등의 기능을 정의합니다.
 */
interface QuestLifecycle {
    /**
     * 퀘스트를 시작합니다.
     * 이 메소드는 퀘스트가 시작될 때 호출되어야 합니다.
     */
    fun questStart()

    /**
     * 퀘스트를 완료합니다.
     * 기본적으로 조건이 충족된다면 자동으로 완료되지만,
     * 수동으로 완료시킬 때 사용됩니다.
     */
    fun questClear()

    /**
     * 퀘스트 보상을 지급합니다.
     * 기본적으로, 퀘스트가 완료되었을 때 자동으로 보상을 제공하지만,
     * 수동으로 플레이어에게 보상을 제공하는 데 사용됩니다.
     */
    fun giveReward()
}

/**
 * 상호작용 가능한 엔티티를 위한 인터페이스.
 * 플레이어가 엔티티와 상호작용할 때 호출되는 메소드를 정의합니다.
 */
interface Interactable {
    /**
     * 플레이어가 NPC와 상호작용할 때 호출됩니다.
     * @param event 플레이어와 NPC 간의 상호작용 이벤트.
     */
    fun onInteract(event: PlayerInteractEntityEvent)
}


/**
 * 퀘스트를 정의하는 클래스
 * @param name 퀘스트의 이름
 * @param description 퀘스트의 설명
 * @param reward 지급할 아이템
 * @param npc 퀘스트를 받을 수 있는 NPC
 * @param completionCondition 퀘스트 성공 트리거
 * @param isStart 퀘스트를 시작했는지 여부
 * @param isClear 퀘스트를 성공했는지 여부
 */
data class Quest(
    val name: String,
    val description: List<String>,
    val reward: List<ItemStack>,
    val npc: Entity,
    val completionCondition: QuestCompletionCondition,
    var isStart: Boolean = false,
    var isClear: Boolean = false,
) : QuestLifecycle {

    /**
     * 이미 정의된 퀘스트를 플레이어가 받은 것으로 설정
     */
    override fun questStart() {
        isStart = true
    }

    /**
     * 이미 시작된 퀘스트를 성공한 것으로 설정하고 보상을 제공
     */
    override fun questClear() {
        if (isStart){
            if (completionCondition.isCompleted()) {
                isClear = true
                giveReward()
                save(this)
            }
        }
    }

    override fun giveReward() {
        for (player in onlinePlayer) {
            for (item in reward) {
                if (player.inventory.firstEmpty() != -1) {
                    player.inventory.addItem(item)
                }
                else{
                    player.world.dropItemNaturally(player.location, item)
                }
            }
        }
    }
}


/**
 * 특정 NPC와의 상호작용을 통해 퀘스트 완료 조건을 확인하는 클래스.
 * @param npc 상호작용을 확인할 NPC 엔티티.
 */
class NPCCondition(private val npc: Entity) : QuestCompletionCondition, Interactable {
    private var hasInteracted = false

    /**
     * 퀘스트 완료 조건이 만족되었는지 확인.
     * @return 플레이어가 NPC와 상호작용했다면 완료한 것으로 간주
     */
    override fun isCompleted(): Boolean = hasInteracted

    /**
     * 퀘스트 고유 식별자
     */
    override fun getIdentifier(): String {
        return "NPCCondition"
    }

    /**
     * 플레이어가 엔티티와 상호작용할 때 호출되는 메소드.
     * @param event 플레이어가 엔티티와 상호작용한 이벤트
     */
    override fun onInteract(event: PlayerInteractEntityEvent) {
        val quest = QuestManager.getNpc(event.rightClicked)
        if (quest != null) {
            if (quest.npc == event.rightClicked && quest.completionCondition is NPCCondition) {
                if (quest.isStart){
                    if (!quest.isClear){
                        quest.completionCondition.onInteract(event)
                        quest.questClear()
                        return
                    }
                } else{
                    quest.questStart()
                }
            }
        }
        quest?.questStart()
    }
}

