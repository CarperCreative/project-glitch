package com.carpercreative.preventthespread.persistence

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.Storage
import com.carpercreative.preventthespread.cancer.BlobIdentifier
import com.carpercreative.preventthespread.cancer.CancerBlob
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState

/**
 * Stores which cancer blob a cancer block at a given [BlockPos] belongs to.
 */
class BlobMembershipPersistentState : PersistentState() {
	private val memberships = hashMapOf<BlockPos, BlobIdentifier>()

	fun getBlobMembershipsEntries(): Set<Map.Entry<BlockPos, BlobIdentifier>> = memberships.entries

	fun setMembership(blockPos: BlockPos, cancerBlob: CancerBlob) {
		setMembership(blockPos, cancerBlob.id)
	}

	fun setMembership(blockPos: BlockPos, id: BlobIdentifier) {
		memberships.compute(blockPos) { _, previousId ->
			if (previousId != null) {
				Storage.cancerBlob.getCancerBlobByIdOrNull(previousId)?.also { cancerBlob ->
					Storage.cancerBlob.incrementCancerousBlockCount(cancerBlob, -1)
				}
			}
			Storage.cancerBlob.getCancerBlobByIdOrNull(id)?.also { cancerBlob ->
				Storage.cancerBlob.incrementCancerousBlockCount(cancerBlob, 1)
			}

			id
		}
		markDirty()
	}

	fun removeMembership(blockPos: BlockPos): CancerBlob? {
		val cancerBlobId = memberships.remove(blockPos)
		val cancerBlob = cancerBlobId?.let(Storage.cancerBlob::getCancerBlobByIdOrNull)

		if (cancerBlob != null) {
			Storage.cancerBlob.incrementCancerousBlockCount(cancerBlob, -1)

			if (cancerBlob.cancerousBlockCount == 0) {
				Storage.spreadDifficulty.incrementDefeatedBlobs()
			}
		}

		markDirty()

		return cancerBlob
	}

	fun getMembershipOrNull(blockPos: BlockPos): BlobIdentifier? {
		return memberships[blockPos]
	}

	fun getNearestMemberOrNull(blockPos: BlockPos, blobPredicate: (cancerBlob: CancerBlob) -> Boolean): BlockPos? {
		val validBlobIds = mutableMapOf<BlobIdentifier, Boolean>()

		return memberships.entries
			.asSequence()
			.filter { (_, blobId) ->
				validBlobIds.computeIfAbsent(blobId) {
					blobPredicate(Storage.cancerBlob.getCancerBlobById(blobId))
				}
			}
			.minByOrNull { (cancerousBlockPos) ->
				cancerousBlockPos.getSquaredDistance(blockPos)
			}
			?.key
	}

	override fun writeNbt(nbt: NbtCompound): NbtCompound {
		nbt.putInt(KEY_VERSION, 1)

		val xArray = IntArray(memberships.size)
		val yArray = IntArray(memberships.size)
		val zArray = IntArray(memberships.size)
		val idArray = IntArray(memberships.size)

		for ((index, membership) in memberships.entries.withIndex()) {
			val (blockPos, id) = membership
			xArray[index] = blockPos.x
			yArray[index] = blockPos.y
			zArray[index] = blockPos.z
			idArray[index] = id
		}

		val memberships = NbtCompound().apply {
			putIntArray(KEY_MEMBERSHIPS_X, xArray)
			putIntArray(KEY_MEMBERSHIPS_Y, yArray)
			putIntArray(KEY_MEMBERSHIPS_Z, zArray)
			putIntArray(KEY_MEMBERSHIPS_ID, idArray)
		}
		nbt.put(KEY_MEMBERSHIPS, memberships)

		return nbt
	}

	companion object {
		private const val KEY_VERSION = "version"
		private const val KEY_MEMBERSHIPS = "memberships"
		private const val KEY_MEMBERSHIPS_X = "x"
		private const val KEY_MEMBERSHIPS_Y = "y"
		private const val KEY_MEMBERSHIPS_Z = "z"
		private const val KEY_MEMBERSHIPS_ID = "id"

		private val type = Type(
			{ BlobMembershipPersistentState() },
			BlobMembershipPersistentState::createFromNbt,
			null,
		)

		fun createFromNbt(nbt: NbtCompound): BlobMembershipPersistentState {
			val state = BlobMembershipPersistentState()

			val memberships = nbt.getCompound(KEY_MEMBERSHIPS)
			val xArray = memberships.getIntArray(KEY_MEMBERSHIPS_X)
			val yArray = memberships.getIntArray(KEY_MEMBERSHIPS_Y)
			val zArray = memberships.getIntArray(KEY_MEMBERSHIPS_Z)
			val idArray = memberships.getIntArray(KEY_MEMBERSHIPS_ID)

			for ((index, id) in idArray.withIndex()) {
				state.memberships[BlockPos(xArray[index], yArray[index], zArray[index])] = id
			}

			return state
		}

		fun ServerWorld.getBlobMembershipPersistentState(): BlobMembershipPersistentState {
			return persistentStateManager.getOrCreate(type, "${PreventTheSpread.MOD_ID}_blobMembership")
		}

		fun ServerWorld.getBlobMembershipOrNull(pos: BlockPos): BlobIdentifier? {
			val blobMembershipPersistentState = getBlobMembershipPersistentState()
			return blobMembershipPersistentState.getMembershipOrNull(pos)
		}
	}
}
