package xyz.xenondevs.invui.item.builder;

import net.minestom.server.color.DyeColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.instance.block.banner.BannerPattern;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BannerPatterns;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class BannerBuilder extends AbstractItemBuilder<BannerBuilder> {

    private List<BannerPatterns.Layer> patterns = new ArrayList<>();

    public BannerBuilder(@NotNull Material material) {
        super(material);
    }

    public BannerBuilder(@NotNull Material material, int amount) {
        super(material, amount);
    }

    public BannerBuilder(@NotNull ItemStack base) {
        super(base);
    }

    @Contract("_ -> this")
    public @NotNull BannerBuilder addPattern(@NotNull BannerPattern pattern) {
        patterns.add(new BannerPatterns.Layer(pattern, DyeColor.WHITE));
        return this;
    }

    @Contract("_, _ -> this")
    public @NotNull BannerBuilder addPattern(@NotNull BannerPattern pattern, @NotNull DyeColor color) {
        patterns.add(new BannerPatterns.Layer(pattern, color));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull BannerBuilder setPatterns(@NotNull List<BannerPatterns.@NotNull Layer> patterns) {
        this.patterns = patterns;
        return this;
    }

    @Contract("-> this")
    public @NotNull BannerBuilder clearPatterns() {
        patterns.clear();
        return this;
    }

    @Contract(value = "_ -> new", pure = true)
    @Override
    public @NotNull ItemStack get(@Nullable String lang) {
        return super.get(lang)
                .with(DataComponents.BANNER_PATTERNS,
                        new BannerPatterns(patterns)
                );
    }

    @Override
    public @NotNull BannerBuilder clone() {
        BannerBuilder builder = super.clone();
        builder.patterns = new ArrayList<>(patterns);
        return builder;
    }

}
