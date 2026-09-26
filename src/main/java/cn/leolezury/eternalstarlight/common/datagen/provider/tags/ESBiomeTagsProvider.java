package cn.leolezury.eternalstarlight.common.datagen.provider.tags;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.data.ESBiomes;
import cn.leolezury.eternalstarlight.common.util.ESTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ESBiomeTagsProvider extends BiomeTagsProvider {
	public ESBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider, EternalStarlight.ID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(ESTags.Biomes.PERMAFROST)
			.add(
				ESBiomes.STARLIGHT_PERMAFROST_FOREST,
				ESBiomes.PERMAFROST_PEAKS
			);
		tag(ESTags.Biomes.HAS_PORTAL_RUINS_COMMON)
			.addTag(Tags.Biomes.IS_PLAINS)
			.addTag(BiomeTags.IS_SAVANNA);
		tag(ESTags.Biomes.HAS_PORTAL_RUINS_FOREST)
			.addTag(BiomeTags.IS_FOREST);
		tag(ESTags.Biomes.HAS_PORTAL_RUINS_DESERT)
			.addTag(Tags.Biomes.IS_DESERT)
			.addTag(BiomeTags.IS_BADLANDS);
		tag(ESTags.Biomes.HAS_PORTAL_RUINS_JUNGLE)
			.addTag(BiomeTags.IS_JUNGLE);
		tag(ESTags.Biomes.HAS_PORTAL_RUINS_COLD)
			.addTag(Tags.Biomes.IS_COLD);
		tag(ESTags.Biomes.HAS_GOLEM_FORGE)
			.add(
				ESBiomes.STARLIGHT_FOREST,
				ESBiomes.STARLIGHT_DENSE_FOREST,
				ESBiomes.UMBRAL_PLAINS,
				ESBiomes.GLIMMER_SCRUBLAND,
				ESBiomes.STARLIGHT_PERMAFROST_FOREST,
				ESBiomes.PERMAFROST_PEAKS,
				ESBiomes.STARLIGHT_TAIGA,
				ESBiomes.SCARLET_FOREST
			);
		tag(ESTags.Biomes.HAS_CURSED_GARDEN)
			.add(
				ESBiomes.STARLIGHT_FOREST,
				ESBiomes.STARLIGHT_DENSE_FOREST,
				ESBiomes.UMBRAL_PLAINS,
				ESBiomes.GLIMMER_SCRUBLAND,
				ESBiomes.STARLIGHT_TAIGA,
				ESBiomes.SCARLET_FOREST
			);
		tag(ESTags.Biomes.HAS_STRANGHOUL_DEN)
			.add(
				ESBiomes.DARK_SWAMP
			);
	}
}