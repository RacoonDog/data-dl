package io.github.racoondog.recipedl.mixin;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(CuttingRecipe.class)
public abstract class CuttingRecipeMixin implements Recipe<Inventory> {
    @Shadow @Final protected Ingredient input;
    @Shadow @Final protected ItemStack output;

    @Override
    public JsonObject serializeToJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", Registries.RECIPE_SERIALIZER.getId(getSerializer()).toString());
        if (!getGroup().isEmpty()) object.addProperty("group", getGroup());
        object.add("ingredient", input.toJson());
        object.addProperty("result", Registries.ITEM.getId(output.getItem()).toString());
        object.addProperty("count", output.getCount());
        return object;
    }
}
