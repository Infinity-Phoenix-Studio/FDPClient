/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.FDPClient.isStarting
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.module.modules.client.GameDetector
import net.ccbluex.liquidbounce.file.FileManager.modulesConfig
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.handler.lang.translation
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addPrint
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.*
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.toLowerCamelCase
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.timing.TickedActions.TickScheduler
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.Value
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard

open class Module(

    val name: String,
    val category: Category,
    defaultKeyBind: Int = Keyboard.KEY_NONE,
    val defaultInArray: Boolean = true, // Used in HideCommand to reset modules visibility.
    private val canBeEnabled: Boolean = true,
    private val forcedDescription: String? = null,

    // Adds spaces between lowercase and uppercase letters (KillAura -> Kill Aura)
    val spacedName: String = name.split("(?<=[a-z])(?=[A-Z])".toRegex()).joinToString(separator = " "),
    val subjective: Boolean = category == Category.VISUAL,
    val gameDetecting: Boolean = canBeEnabled,
    val hideModule: Boolean = false,

) : MinecraftInstance(), Listenable {

    // Value that determines whether the module should depend on GameDetector
    private val onlyInGameValue = BoolValue("OnlyInGame", true, subjective = true) { GameDetector.state }

    protected val TickScheduler = TickScheduler(this)

    // List to register additional options from classes
    private val configurables = mutableListOf<Class<*>>()
    fun addConfigurable(provider: Any) {
        configurables += provider::class.java
    }

    // Module information

    // Get normal or spaced name
    fun getName(spaced: Boolean = Arraylist.spacedModules) = if (spaced) spacedName else name

    var keyBind = defaultKeyBind
        set(keyBind) {
            field = keyBind

            saveConfig(modulesConfig)
        }

    val hideModuleValue: BoolValue = object : BoolValue("Hide", false, subjective = true) {
        override fun onUpdate(value: Boolean) {
            inArray = !value
        }
    }

    // Use for synchronizing
    val hideModuleValues: BoolValue = object : BoolValue("HideSync", hideModuleValue.get(), subjective = true) {
        override fun onUpdate(value: Boolean) {
            hideModuleValue.set(value)
        }
    }

    var inArray = defaultInArray
        set(value) {
            field = value

            saveConfig(modulesConfig)
        }

    val description
        get() = forcedDescription ?: translation("module.${name.toLowerCamelCase()}.description")

    var slideStep = 0F

    // Current state of module
    var state = false
        set(value) {
            if (field == value)
                return

            // Call toggle
            onToggle(value)

            TickScheduler.clear()

            // Play sound and add notification
            if (!isStarting) {
                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.click"), 1F))
                addNotification(Notification(name,"${if (value) "Enabled" else "Disabled"} §r$name", if (value) Type.SUCCESS else Type.ERROR, 1000))
                addPrint(
                    Prints.Print(
                        "${if (value) "Enabled" else "Disabled"} §r$name",
                        1000f,
                        if (value) PrintType.SUCCESS else PrintType.ERROR
                    )
                )
            }

            // Call on enabled or disabled
            if (value) {
                onEnable()

                if (canBeEnabled)
                    field = true
            } else {
                onDisable()
                field = false
            }

            // Save module state
            saveConfig(modulesConfig)
        }


    // HUD
    val hue = nextFloat()
    var slide = 0F
    var yAnim = 0f

    // Tag
    open val tag: String?
        get() = null

    /**
     * Toggle module
     */
    fun toggle() {
        state = !state
    }

    /**
     * Called when module initialized
     */
    open fun onInitialize() {}

    /**
     * Called when module toggled
     */
    open fun onToggle(state: Boolean) {}

    /**
     * Called when module enabled
     */
    open fun onEnable() {}

    /**
     * Called when module disabled
     */
    open fun onDisable() {}

    /**
     * Get value by [valueName]
     */
    open fun getValue(valueName: String) = values.find { it.name.equals(valueName, ignoreCase = true) }

    /**
     * Get value via `module[valueName]`
     */
    operator fun get(valueName: String) = getValue(valueName)

    /**
     * Get all values of module with unique names
     */
    open val values: Set<Value<*>>
        get() {
            var orderedValues = mutableSetOf<Value<*>>()

            javaClass.declaredFields.forEach { innerField ->
                innerField.isAccessible = true
                val element = innerField[this] ?: return@forEach

                orderedValues = ClassUtils.findValues(element, configurables, orderedValues)
            }

            if (gameDetecting) orderedValues += onlyInGameValue
            if (!hideModule) orderedValues += hideModuleValue

            return orderedValues
        }

    val isActive
        get() = !gameDetecting || !onlyInGameValue.get() || GameDetector.isInGame()

    /**
     * Events should be handled when module is enabled
     */
    override fun handleEvents() = state && isActive
}
