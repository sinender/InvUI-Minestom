package xyz.xenondevs.invui.item.builder;

import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.PotionType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PotionBuilder extends AbstractItemBuilder<PotionBuilder> {

    private List<CustomPotionEffect> effects = new ArrayList<>();
    private Color color;
    private PotionContents basePotionData;

    public PotionBuilder(@NotNull PotionContents type) {
        super(ItemStack.builder(Material.POTION)
                .set(DataComponents.POTION_CONTENTS, type)
                .build()
        );
    }

    public PotionBuilder(@NotNull ItemStack base) {
        super(base);
    }

    @Contract("_ -> this")
    public @NotNull PotionBuilder setColor(@NotNull Color color) {
        this.color = color;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull PotionBuilder setColor(@NotNull java.awt.Color color) {
        this.color = new Color(color.getRed(), color.getGreen(), color.getBlue());
        return this;
    }

    @Contract("_ -> this")
    public @NotNull PotionBuilder setBasePotionData(@NotNull PotionContents basePotionData) {
        this.basePotionData = basePotionData;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull PotionBuilder addEffect(@NotNull CustomPotionEffect effect) {
        effects.add(effect);
        return this;
    }

    @Contract(value = "_ -> new", pure = true)
    @Override
    public @NotNull ItemStack get(@Nullable String lang) {
        ItemStack item = super.get(lang);
        if (basePotionData != null) {
            item = item.with(DataComponents.POTION_CONTENTS, basePotionData);
        } else {
            item = item.with(DataComponents.POTION_CONTENTS, new PotionContents(PotionType.AWKWARD, color, effects));
        }
        return item;
    }

    @Override
    public @NotNull PotionBuilder clone() {
        PotionBuilder builder = super.clone();
        builder.effects = new ArrayList<>(effects);
        return builder;
    }
}
