package com.carpercreative.preventthespread.persistence

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.cancer.BlobIdentifier
import com.carpercreative.preventthespread.cancer.CancerBlob
import com.carpercreative.preventthespread.cancer.CancerBlob.Companion.toCancerBlob
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentState
import net.minecraft.world.World

class CancerBlobPersistentState : PersistentState() {
	private var nextId = 0

	private val cancerBlobs = hashMapOf<BlobIdentifier, CancerBlob>()

	fun getCancerBlobByIdOrNull(id: BlobIdentifier): CancerBlob? {
		return cancerBlobs[id]
	}

	fun getCancerBlobById(id: BlobIdentifier): CancerBlob {
		return getCancerBlobByIdOrNull(id)
			?: throw IllegalArgumentException("Requested cancer blob with id $id, which does not exist.")
	}

	fun <T : CancerBlob?> createCancerBlob(callback: (id: BlobIdentifier) -> T): T {
		val id = getNextId()
		val cancerBlob = callback(id)
		cancerBlob?.also {
			cancerBlobs[id] = cancerBlob
			markDirty()
		}
		return cancerBlob
	}

	fun removeCancerBlob(id: BlobIdentifier): CancerBlob? {
		val cancerBlob = cancerBlobs.remove(id)
		if (cancerBlob != null) {
			markDirty()
		}
		return cancerBlob
	}

	private fun getNextId(): BlobIdentifier {
		val id = nextId++
		markDirty()
		return id
	}

	override fun writeNbt(nbt: NbtCompound): NbtCompound {
		nbt.putInt(KEY_VERSION, 1)

		nbt.putInt(KEY_NEXT_ID, nextId)

		val cancerBlobsNbt = NbtList()
		for (cancerBlob in cancerBlobs.values) {
			cancerBlobsNbt.add(cancerBlob.toNbt())
		}
		nbt.put(KEY_CANCER_BLOBS, cancerBlobsNbt)

		return nbt
	}

	companion object {
		private const val KEY_VERSION = "version"
		private const val KEY_NEXT_ID = "nextId"
		private const val KEY_CANCER_BLOBS = "cancerBlobs"

		private val type = Type(
			{ CancerBlobPersistentState() },
			CancerBlobPersistentState::createFromNbt,
			null,
		)

		fun createFromNbt(nbt: NbtCompound): CancerBlobPersistentState {
			val state = CancerBlobPersistentState()

			state.nextId = nbt.getInt(KEY_NEXT_ID)

			val cancerBlobsNbt = nbt.getList(KEY_CANCER_BLOBS, NbtElement.COMPOUND_TYPE.toInt())
			for (cancerBlobNbt in cancerBlobsNbt) {
				val cancerBlob = (cancerBlobNbt as NbtCompound).toCancerBlob()
				state.cancerBlobs[cancerBlob.id] = cancerBlob
			}

			return state
		}

		fun ServerWorld.getCancerBlobPersistentState(): CancerBlobPersistentState {
			return server.getWorld(World.OVERWORLD)!!.persistentStateManager.getOrCreate(type, "${PreventTheSpread.MOD_ID}_cancerBlob")
		}
	}
}