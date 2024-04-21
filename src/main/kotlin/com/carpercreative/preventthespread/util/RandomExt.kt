package com.carpercreative.preventthespread.util

import net.minecraft.util.math.random.Random

fun <T> Random.nextOfListOrNull(list: List<T>): T? {
	if (list.isEmpty()) return null
	val index = nextInt(list.size)
	return list[index]
}

fun <T> Random.nextOfList(list: List<T>): T {
	return nextOfListOrNull(list)
		?: throw IllegalArgumentException("List is empty.")
}

fun <T> Random.nextOfArrayOrNull(array: Array<T>): T? {
	if (array.isEmpty()) return null
	val index = nextInt(array.size)
	return array[index]
}

fun <T> Random.nextOfArray(array: Array<T>): T {
	return nextOfArrayOrNull(array)
		?: throw IllegalArgumentException("Array is empty.")
}
