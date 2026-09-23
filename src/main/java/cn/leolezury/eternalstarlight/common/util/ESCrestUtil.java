package cn.leolezury.eternalstarlight.common.util;

import cn.leolezury.eternalstarlight.common.crest.Crest;
import cn.leolezury.eternalstarlight.common.data.ESRegistries;
import cn.leolezury.eternalstarlight.common.network.ParticlePacket;
import cn.leolezury.eternalstarlight.common.particle.OrbitalTrailParticleOptions;
import cn.leolezury.eternalstarlight.common.particle.SurroundingTrailParticleOptions;
import cn.leolezury.eternalstarlight.common.platform.ESPlatform;
import cn.leolezury.eternalstarlight.common.registry.ESDataAttachments;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ESCrestUtil {
	public static List<Crest.Instance> getCrests(Player player) {
		return ESDataAttachments.CRESTS.getData(player);
	}

	public static List<Crest.Instance> getOwnedCrests(Player player) {
		return ESDataAttachments.OWNED_CRESTS.getData(player);
	}

	public static void setCrests(Player player, List<Crest.Instance> crests) {
		ESDataAttachments.CRESTS.setData(player, crests);
	}

	public static void setOwnedCrests(Player player, List<Crest.Instance> crests) {
		ESDataAttachments.OWNED_CRESTS.setData(player, crests);
	}

	public static boolean giveCrest(Player player, Crest.Instance crest) {
		List<Crest.Instance> set = getOwnedCrests(player);
		List<Crest.Instance> crests = new ArrayList<>(set);

		ResourceKey<Crest> key = crest.crest()
			.unwrapKey()
			.orElseThrow(() -> new IllegalStateException("Crest holder has no key: " + crest));

		for (Crest.Instance instance : crests) {
			if (instance.crest().is(key) && instance.level() >= crest.level()) {
				return false;
			}
		}

		crests.removeIf(c -> c.crest().is(key));

		crests.add(crest);
		setOwnedCrests(player, crests);
		return true;
	}

	public static boolean upgradeCrest(Player player, ResourceKey<Crest> key) {
		int maxLevel = 0;
		Optional<Holder.Reference<Crest>> holder = player.level().registryAccess().registryOrThrow(ESRegistries.CREST).getHolder(key);
		if (holder.isPresent()) {
			maxLevel = holder.get().value().maxLevel();
		}
		if (getCrestLevel(player, key) + 1 > maxLevel) {
			return false;
		}
		Optional<Crest.Instance> instance = Crest.Instance.of(player.level().registryAccess(), key, getCrestLevel(player, key) + 1);
		return instance.isPresent() && giveCrest(player, instance.get());
	}

	public static boolean removeCrest(Player player, Holder<Crest> crest) {
		removeCrest(player, crest, false);
		return removeCrest(player, crest, true);
	}

	public static boolean removeCrest(Player player, Holder<Crest> crest, boolean owned) {
		List<Crest.Instance> set = owned ? getOwnedCrests(player) : getCrests(player);
		List<Crest.Instance> crests = new ArrayList<>(set);

		ResourceKey<Crest> key = crest.unwrapKey()
			.orElseThrow(() -> new IllegalStateException("Crest holder has no key: " + crest));

		if (crests.stream().noneMatch(c -> c.crest().is(key))) {
			return false;
		}

		crests.removeIf(c -> c.crest().is(key));

		if (owned) {
			setOwnedCrests(player, crests);
		} else {
			setCrests(player, crests);
		}

		return true;
	}

	public static List<Crest.Instance> mergeCrests(List<Crest.Instance> first, List<Crest.Instance> second) {
		List<Crest.Instance> result = new ArrayList<>(first);

		for (Crest.Instance instance : second) {

			ResourceKey<Crest> key = instance.crest().unwrapKey().orElseThrow(() -> new IllegalStateException("Crest holder has no key: " + instance));

			boolean hasSame = false;
			int level = instance.level();

			for (int i = 0; i < result.size(); i++) {
				Crest.Instance existing = result.get(i);

				if (existing.crest().is(key)) {
					level = Math.max(level, existing.level());
					result.set(i, new Crest.Instance(instance.crest(), level));
					hasSame = true;
				}
			}

			if (!hasSame) {
				result.add(instance);
			}
		}

		return result;
	}

	public static int getCrestLevel(Player player, ResourceKey<Crest> key) {
		Optional<Holder.Reference<Crest>> crest = player.level().registryAccess().registryOrThrow(ESRegistries.CREST).getHolder(key);
		return crest.map(ref -> getCrestLevel(player, ref)).orElse(0);
	}

	public static int getCrestLevel(Player player, Holder<Crest> crest) {
		ResourceKey<Crest> key = crest.unwrapKey().orElseThrow(() -> new IllegalStateException("Crest holder has no key: " + crest));

		for (Crest.Instance instance : getOwnedCrests(player)) {
			if (instance.crest().is(key)) {
				return instance.level();
			}
		}

		return 0;
	}

	public static void tickCrests(Player player) {
		List<Crest.Instance> ownedSet = getOwnedCrests(player);
		List<Crest.Instance> set = getCrests(player);

		ItemStack mainHand = player.getMainHandItem();
		ItemStack offHand  = player.getOffhandItem();

		Crest.Instance mainInst = Crest.Instance.get(mainHand);
		if (mainInst != null) {
			Holder<Crest> component = mainInst.crest();

			boolean owned = ownedSet.stream().anyMatch(c ->
				c.crest().unwrapKey().orElseThrow().location()
					.equals(component.unwrapKey().orElseThrow().location())
			);

			if (component.isBound() && !owned) {
				mainHand.removeTagKey(Crest.Instance.TAG_CREST);
			}

		} else {
			Crest.Instance offInst = Crest.Instance.get(offHand);
			if (offInst != null) {
				Holder<Crest> component = offInst.crest();

				boolean owned = ownedSet.stream().anyMatch(c ->
					c.crest().unwrapKey().orElseThrow().location()
						.equals(component.unwrapKey().orElseThrow().location())
				);

				if (component.isBound() && !owned) {
					offHand.removeTagKey(Crest.Instance.TAG_CREST);
				}
			}
		}

		List<Crest.Instance> oldActiveCrests = new ArrayList<>(ESDataAttachments.OLD_ACTIVE_CRESTS.getData(player));
		List<Crest.Instance> activeCrests = new ArrayList<>();
		set.forEach(crest -> {
			boolean doEffects = false;
			if (player.getAbilities().instabuild) {
				doEffects = true;
			} else {
				Inventory inventory = player.getInventory();
				for (int i = 0; i < inventory.getContainerSize(); i++) {
					ItemStack stack = inventory.getItem(i);
					if (stack.is(crest.crest().value().type().getCrystalsTag())) {
						doEffects = true;
						if (player.tickCount % 60 == 0) {
							stack.hurtAndBreak(crest.level(), player, p -> LivingEntity.getEquipmentSlotForItem(stack));
						}
						break;
					}
				}
			}
			if (doEffects) {
				activeCrests.add(crest);
				crest.crest().value().effects().ifPresent(effects ->
					effects.forEach(mobEffect -> player.addEffect(new MobEffectInstance(mobEffect.effect().value(), 20, mobEffect.level() + (crest.level() - 1) * mobEffect.levelAddition())))
				);
				crest.crest().value().attributeModifiers().ifPresent(modifiers ->
					modifiers.forEach(modifier -> {
						AttributeInstance instance = player.getAttributes().getInstance(modifier.attribute());
						AttributeModifier attrModifier = modifier.getModifier(crest.level());
						if (instance != null && !instance.hasModifier(attrModifier)) {
							instance.addPermanentModifier(attrModifier);
						}
					})
				);
			}
		});
		oldActiveCrests.removeAll(activeCrests);
		for (Crest.Instance crest : oldActiveCrests) {
			crest.crest().value().attributeModifiers().ifPresent(modifiers ->
				modifiers.forEach(modifier -> {
					AttributeInstance instance = player.getAttributes().getInstance(modifier.attribute());
					AttributeModifier attrModifier = modifier.getModifier(crest.level());
					if (instance != null && instance.hasModifier(attrModifier)) {
						instance.removeModifier(attrModifier.getId());
					}
				})
			);
		}
		ESDataAttachments.OLD_ACTIVE_CRESTS.setData(player, activeCrests);
		if (!set.isEmpty() && player.level() instanceof ServerLevel serverLevel && player.tickCount % 100 == 0) {
			ESPlatform.INSTANCE.sendToAllClients(serverLevel, new ParticlePacket(SurroundingTrailParticleOptions.magic(player), player.getX(), player.getY(), player.getZ(), 0, 0.02, 0));
		}
	}
}
