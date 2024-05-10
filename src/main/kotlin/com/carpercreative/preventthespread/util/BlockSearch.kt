package com.carpercreative.preventthespread.util

import com.carpercreative.preventthespread.cancer.CancerLogic.isCancerous
import java.util.LinkedList
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.WorldAccess

object BlockSearch {
	fun findBlocks(world: WorldAccess, startPos: BlockPos, limit: Int, predicate: (state: BlockState) -> Boolean): List<BlockPos> {
		if (limit == 1) {
			return when {
				world.getBlockState(startPos).let(predicate) -> listOf(startPos.toImmutable())
				else -> emptyList()
			}
		}

		val visited = hashSetOf<BlockPos>()
		val found = mutableListOf<BlockPos>()
		val candidates = LinkedList<BlockPos>()

		candidates.add(startPos)
		visited.add(startPos)

		while (candidates.isNotEmpty()) {
			val pos = candidates.pop()

			val state = world.getBlockState(pos)
			if (!predicate(state)) continue

			found.add(pos)
			if (found.size >= limit) break

			for (direction in Direction.entries) {
				val offsetPos = pos.offset(direction)

				if (!visited.contains(offsetPos)) {
					visited.add(offsetPos)
					candidates.add(offsetPos)
				}
			}
		}

		return found
	}

	fun findCancerousBlocks(world: WorldAccess, startPos: BlockPos, limit: Int): List<BlockPos> {
		return findBlocks(world, startPos, limit) { state -> state.isCancerous() }
	}
}
