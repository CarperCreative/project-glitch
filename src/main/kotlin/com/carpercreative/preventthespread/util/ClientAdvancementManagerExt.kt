package com.carpercreative.preventthespread.util

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.advancement.AdvancementEntry
import net.minecraft.advancement.AdvancementProgress
import net.minecraft.client.network.ClientAdvancementManager

@get:Environment(EnvType.CLIENT)
private val CLIENT_ADVANCEMENT_MANAGER_ADVANCEMENT_PROGRESSES by lazy {
	ClientAdvancementManager::class.java.getDeclaredField("advancementProgresses").apply { trySetAccessible() }
}

@get:Environment(EnvType.CLIENT)
@Suppress("UNCHECKED_CAST")
val ClientAdvancementManager.advancementProgresses_accessor get() = CLIENT_ADVANCEMENT_MANAGER_ADVANCEMENT_PROGRESSES.get(this) as Map<AdvancementEntry, AdvancementProgress>
