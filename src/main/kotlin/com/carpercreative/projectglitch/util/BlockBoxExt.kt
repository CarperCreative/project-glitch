package com.carpercreative.projectglitch.util

import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos

/**
 * Returns a [Sequence] of [mutable][BlockPos.Mutable] positions of all blocks contained within this [BlockBox].
 */
fun BlockBox.contentsSequence() = sequence<BlockPos> {
	val mutableBlockPos = BlockPos.Mutable()

	for (x in minX..maxX) {
		for (y in minY..maxY) {
			for (z in minZ..maxZ) {
				mutableBlockPos.set(x, y, z)
				yield(mutableBlockPos)
			}
		}
	}
}
