package cn.leolezury.eternalstarlight.common.item.loot;

import cn.leolezury.eternalstarlight.common.registry.ESLootItemConditions;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record BossChallengeCountCondition(ResourceLocation bossId, Optional<Integer> minInclusive, Optional<Integer> maxInclusive) implements LootItemCondition {

	@Override
	public LootItemConditionType getType() {
		return ESLootItemConditions.BOSS_CHALLENGE_COUNT.get();
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(ESLootContextParams.BOSS_CHALLENGE_COUNTS);
	}

	@Override
	public boolean test(LootContext context) {
		Map<ResourceLocation, Integer> counts = context.getParamOrNull(ESLootContextParams.BOSS_CHALLENGE_COUNTS);
		if (counts != null) {
			int count = counts.getOrDefault(bossId, 0);
			boolean result = true;
			if (minInclusive.isPresent()) {
				result = count >= minInclusive.get();
			}
			if (maxInclusive.isPresent()) {
				result = result && count <= maxInclusive.get();
			}
			return result;
		}
		return false;
	}

	public static LootItemCondition.Builder min(ResourceLocation bossId, int min) {
		return () -> new BossChallengeCountCondition(bossId, Optional.of(min), Optional.empty());
	}

	public static LootItemCondition.Builder max(ResourceLocation bossId, int max) {
		return () -> new BossChallengeCountCondition(bossId, Optional.empty(), Optional.of(max));
	}

	public static LootItemCondition.Builder range(ResourceLocation bossId, int min, int max) {
		return () -> new BossChallengeCountCondition(bossId, Optional.of(min), Optional.of(max));
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<BossChallengeCountCondition> {
		@Override
		public void serialize(JsonObject json, BossChallengeCountCondition condition, JsonSerializationContext context) {
			json.addProperty("boss_id", condition.bossId().toString());
			condition.minInclusive().ifPresent(min -> json.addProperty("min_inclusive", min));
			condition.maxInclusive().ifPresent(max -> json.addProperty("max_inclusive", max));
		}

		@Override
		public BossChallengeCountCondition deserialize(JsonObject json, JsonDeserializationContext context) {
			ResourceLocation bossId = new ResourceLocation(GsonHelper.getAsString(json, "boss_id"));
			Optional<Integer> minInclusive = json.has("min_inclusive")
				? Optional.of(GsonHelper.getAsInt(json, "min_inclusive"))
				: Optional.empty();
			Optional<Integer> maxInclusive = json.has("max_inclusive")
				? Optional.of(GsonHelper.getAsInt(json, "max_inclusive"))
				: Optional.empty();
			return new BossChallengeCountCondition(bossId, minInclusive, maxInclusive);
		}
	}
}