package com.carpercreative.projectglitch.mixin;

import com.carpercreative.projectglitch.ProjectGlitch;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {
	@Redirect(
		method = "updateResult(Lnet/minecraft/screen/ScreenHandler;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/inventory/RecipeInputInventory;Lnet/minecraft/inventory/CraftingResultInventory;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/inventory/CraftingResultInventory;shouldCraftRecipe(Lnet/minecraft/world/World;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/recipe/RecipeEntry;)Z"
		)
	)
	private static boolean updateResult$shouldCraftRecipe(CraftingResultInventory instance, World world, ServerPlayerEntity player, RecipeEntry<?> recipeEntry) {
		if (
			!player.getRecipeBook().contains(recipeEntry)
			&& recipeEntry.value().getResult(world.getRegistryManager()).isIn(ProjectGlitch.INSTANCE.getREQUIRES_RECIPE_TO_CRAFT_ITEM_TAG())
		) {
			return false;
		}

		return instance.shouldCraftRecipe(world, player, recipeEntry);
	}
}
