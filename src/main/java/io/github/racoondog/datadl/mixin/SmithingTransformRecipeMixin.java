package io.github.racoondog.datadl.mixin;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(SmithingTransformRecipe.class)
public abstract class SmithingTransformRecipeMixin implements SmithingRecipe {
    @Shadow @Final Ingredient template;
    @Shadow @Final Ingredient base;
    @Shadow @Final Ingredient addition;
    @Shadow @Final ItemStack result;

    @Override
    public JsonObject serializeToJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", Registries.RECIPE_SERIALIZER.getId(getSerializer()).toString());
        object.add("template", template.toJson());
        object.add("base", base.toJson());
        object.add("addition", addition.toJson());
        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("item", Registries.ITEM.getId(result.getItem()).toString());
        if (result.getCount() != 1) resultJson.addProperty("count", result.getCount());
        object.add("result", resultJson);
        return object;
    }
}
