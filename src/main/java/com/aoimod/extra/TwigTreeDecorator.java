package com.aoimod.extra;

import com.aoimod.blocks.ModBlocks;
import com.aoimod.blocks.Twig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PlantBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

import java.util.Comparator;

public class TwigTreeDecorator extends TreeDecorator {
    public static final MapCodec<TwigTreeDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("type")
                    .forGetter(decorator -> decorator.type.toString())
    ).apply(instance, s -> new TwigTreeDecorator(Twig.TwigTypeEnum.getOrCreate(s))));

    private final Twig.TwigTypeEnum type;

    public TwigTreeDecorator(Twig.TwigTypeEnum type) {
        this.type = type;
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return ModTreeDecoratorTypes.TWIG_TREE_DECORATOR_TYPE;
    }

    @Override
    public void generate(Generator generator) {
        if (!Twig.TwigTypeEnum.record.containsValue(type))
            return;

        generator.getLogPositions().stream().min(Comparator.comparingInt(Vec3i::getY)).ifPresent(bottom -> {
            var world = generator.getWorld();
            var random = generator.getRandom();
            for (int i = 0, times = 3; i < times; i++) {
                int x = random.nextBetween(-3, 3),
                    z = random.nextBetween(-3, 3);

                BlockPos pos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, bottom.add(x, 0, z));
                if (world.testBlockState(pos.down(), state -> state.isIn(TagKey.of(Registries.BLOCK.getKey(), Identifier.of("logs"))) || !state.isOpaqueFullCube()) ||
                    world.testFluidState(pos, state -> !state.isEmpty() && !state.isOf(Fluids.WATER)))
                    continue;

                generator.replace(pos, ModBlocks.TWIG.getDefaultState()
                        .with(Twig.TWIG_TYPE, type)
                        .with(Twig.FACING, Direction.Type.HORIZONTAL.random(random)));
            }
        });
    }
}
