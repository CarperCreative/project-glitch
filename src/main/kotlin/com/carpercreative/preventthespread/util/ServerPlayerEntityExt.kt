package com.carpercreative.preventthespread.util

import net.minecraft.advancement.AdvancementEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

/**
 * @return `false` if the advancement doesn't exist or is already granted, `true` otherwise.
 */
fun ServerPlayerEntity.grantAdvancement(advancementId: Identifier): Boolean {
	val advancementEntry = server.advancementLoader.get(advancementId)
		?: return false
	return grantAdvancement(advancementEntry)
}

/**
 * @return `true` if the advancement has been granted, `false` otherwise.
 */
fun ServerPlayerEntity.grantAdvancement(advancementEntry: AdvancementEntry): Boolean {
	if (advancementTracker.getProgress(advancementEntry).isDone) return false

	for (criterion in advancementEntry.value.criteria) {
		advancementTracker.grantCriterion(advancementEntry, criterion.key)
	}
	return true
}
