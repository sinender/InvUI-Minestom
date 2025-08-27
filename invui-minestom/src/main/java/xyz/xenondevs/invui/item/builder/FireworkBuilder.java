package xyz.xenondevs.invui.item.builder;

import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.FireworkExplosion;
import net.minestom.server.item.component.FireworkList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

public final class FireworkBuilder extends AbstractItemBuilder<FireworkBuilder> {

    private int power = -1;
    private List<FireworkExplosion> effects = new ArrayList<>();

    public FireworkBuilder() {
        super(Material.FIREWORK_ROCKET);
    }

    public FireworkBuilder(int amount) {
        super(Material.FIREWORK_ROCKET, amount);
    }

    public FireworkBuilder(@NotNull ItemStack base) {
        super(base);
    }

    @Contract("_ -> this")
    public @NotNull FireworkBuilder setPower(@Range(from = 0, to = 127) int power) {
        this.power = power;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull FireworkBuilder addFireworkEffect(@NotNull FireworkExplosion effect) {
        effects.add(effect);
        return this;
    }

    @Contract("_ -> this")
    public @NotNull FireworkBuilder setFireworkEffects(@NotNull List<@NotNull FireworkExplosion> effects) {
        this.effects = effects;
        return this;
    }

    @Contract("-> this")
    public @NotNull FireworkBuilder clearFireworkEffects() {
        effects.clear();
        return this;
    }

    @Contract(value = "_ -> new", pure = true)
    @Override
    public @NotNull ItemStack get(@Nullable String lang) {
        return super.get(lang)
                .with(DataComponents.FIREWORKS,
                        new FireworkList(power, effects)
                );
    }

    @Override
    public @NotNull FireworkBuilder clone() {
        FireworkBuilder builder = super.clone();
        builder.effects = new ArrayList<>(effects);
        return builder;
    }

}
