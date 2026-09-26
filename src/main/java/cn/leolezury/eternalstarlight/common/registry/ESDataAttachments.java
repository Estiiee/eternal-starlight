package cn.leolezury.eternalstarlight.common.registry;

import cn.leolezury.eternalstarlight.common.crest.Crest;
import cn.leolezury.eternalstarlight.common.platform.ESPlatform;
import cn.leolezury.eternalstarlight.common.platform.EntityDataAttachment;
import cn.leolezury.eternalstarlight.common.spell.SpellCastData;
import cn.leolezury.eternalstarlight.common.spell.SpellCooldown;
import cn.leolezury.eternalstarlight.common.util.SpecialItemCooldown;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

//it's more likely for me to win a lottery than for this code to actually work
public class ESDataAttachments {
	private static final List<EntityDataAttachment<?>> ATTACHMENTS = new ArrayList<>();

	public static final EntityDataAttachment<Vec3> MOVEMENT =
		regVec3("movement", Vec3.ZERO, false);

	public static final EntityDataAttachment<Integer> LAST_MOVEMENT_UPDATE =
		regInt("last_movement_update", 0, false);

	public static final EntityDataAttachment<LivingEntity> CONCENTRATED_TARGET =
		regSimple("concentrated_target", () -> null, false,
			(buf, v) -> {}, // not synced
			buf -> null);

	public static final EntityDataAttachment<ItemStack> CONCENTRATED_WEAPON =
		regItemStack("concentrated_weapon", false);

	public static final EntityDataAttachment<Integer> LAST_CONCENTRATED_ATTACK_TIME =
		regInt("last_concentrated_attack_time", Integer.MIN_VALUE, false);

	public static final EntityDataAttachment<Integer> CONCENTRATION_LEVEL =
		regInt("concentration_level", 0, false);

	public static final EntityDataAttachment<BlockPos> FLOWGLAZE_DESTROY_BLOCK_TARGET =
		regBlockPos("flowglaze_destroy_block_target", false);

	public static final EntityDataAttachment<Integer> FLOWGLAZE_DESTROY_BLOCK_TICKS =
		regInt("flowglaze_destroy_block_ticks", 0, false);

	public static final EntityDataAttachment<Boolean> IN_ETHER =
		regBool("in_ether", false, false);

	public static final EntityDataAttachment<Integer> IN_ETHER_TICKS =
		regInt("in_ether_ticks", 0, false);

	public static final EntityDataAttachment<Boolean> OBTAINED_BLOSSOM_OF_STARS =
		regBool("obtained_blossom_of_stars", false, true);

	public static final EntityDataAttachment<Float> NUMBNESS_DAMAGE =
		regFloat("numbness_damage", 0f, false);

	public static final EntityDataAttachment<Integer> TEARY_TICKS =
		regInt("teary_ticks", 0, false);

	public static final EntityDataAttachment<Integer> ABYSSAL_FIRE_TICKS =
		regInt("abyssal_fire_ticks", 0, false);

	public static final EntityDataAttachment<String> ARROW_TYPE =
		regString("arrow_type", "", false);

	public static final EntityDataAttachment<Float> FLOWGLAZE_ARROW_EXTRA_BASE_DAMAGE =
		regFloat("flowglaze_arrow_extra_base_damage", 0f, false);

	public static final EntityDataAttachment<Integer> METEOR_COOLDOWN =
		regInt("meteor_cooldown", 0, false);

	public static final EntityDataAttachment<Integer> HUSK_OWNER_ID =
		regInt("husk_owner_id", -1, false);

	public static final EntityDataAttachment<Map<ResourceLocation, Integer>> BOSS_CHALLENGE_COUNT =
		regSimple("boss_challenge_count", Map::of, true,
			(buf, map) -> {
				buf.writeVarInt(map.size());
				for (Map.Entry<ResourceLocation, Integer> entry : map.entrySet()) {
					buf.writeResourceLocation(entry.getKey());
					buf.writeVarInt(entry.getValue());
				}
			},
			buf -> {
				int size = buf.readVarInt();
				Map<ResourceLocation, Integer> map = new HashMap<>();
				for (int i = 0; i < size; i++) {
					ResourceLocation key = buf.readResourceLocation();
					int value = buf.readVarInt();
					map.put(key, value);
				}
				return Map.copyOf(map);
			});

	public static final EntityDataAttachment<Integer> STRANGHOUL_HIRING_COOLDOWN =
		regInt("stranghoul_hiring_cooldown", 0, true);

	public static final EntityDataAttachment<Integer> BOARWARF_CREDIT =
		regInt("boarwarf_credit", 0, true);

	public static final EntityDataAttachment<Boolean> CRESCENT_SPEAR_DASH =
		regBool("crescent_spear_dash", false, false);

	public static final EntityDataAttachment<GlobalPos> ENERGY_TRANSMITTER_SOURCE =
		regSimple("energy_transmitter_source", () -> null, false,
			(buf, v) -> {}, // not synced
			buf -> null);

	public static final EntityDataAttachment<Boolean> OFFHAND_ATTACK =
		regBool("offhand_attack", false, false);

	public static final EntityDataAttachment<ItemStack> LAST_OFFHAND_ITEM =
		regItemStack("last_offhand_item", false);

	public static final EntityDataAttachment<Integer> OFFHAND_ATTACK_STRENGTH_TIMER =
		regInt("offhand_attack_strength_timer", 0, false);

	public static final EntityDataAttachment<Integer> GRAPPLING =
		regInt("grappling", -1, false);

	public static final EntityDataAttachment<Integer> WHIP =
		regInt("whip", -1, false);

	public static final EntityDataAttachment<List<String>> GUIDEBOOK_LISTENING_NAMESPACES =
		regSimple("guidebook_listening_namespaces", List::of, true,
			(buf, v) -> {
				buf.writeVarInt(v.size());
				for (String s : v) buf.writeUtf(s);
			},
			buf -> {
				int size = buf.readVarInt();
				List<String> list = new ArrayList<>(size);
				for (int i = 0; i < size; i++) list.add(buf.readUtf());
				return list;
			});

	public static final EntityDataAttachment<Boolean> IMPORTANT_ITEM =
		regBool("important_item", false, false);

	public static final EntityDataAttachment<Integer> CRYSTAL_GREATSWORD_MUSIC_INDEX =
		regInt("crystal_greatsword_music_index", 0, false);

	public static final EntityDataAttachment<Boolean> RECEIVED_GUIDEBOOK =
		regBool("received_guidebook", false, true);



	public static final EntityDataAttachment<SpellCastData> SPELL_CAST_DATA =
		regSimple("spell_cast_data", SpellCastData::getDefault, false,
			(buf, v) -> v.write(buf),
			SpellCastData::read);

	public static final EntityDataAttachment<List<SpellCooldown>> SPELL_COOLDOWNS =
		regSimple("spell_cooldowns", List::of, false,
			(buf, list) -> {
				buf.writeVarInt(list.size());
				for (SpellCooldown cd : list) cd.write(buf);
			},
			buf -> {
				int size = buf.readVarInt();
				List<SpellCooldown> list = new ArrayList<>(size);
				for (int i = 0; i < size; i++) list.add(SpellCooldown.read(buf));
				return list;
			});

	public static final EntityDataAttachment<SpellCastData.SpellSource> SPELL_SOURCE =
		regSimple("spell_source", () -> e -> false, false,
			(buf, v) -> {}, // not synced
			buf -> e -> false);

	public static final EntityDataAttachment<List<Crest.Instance>> OLD_ACTIVE_CRESTS =
		regSimple("old_active_crests", List::of, true,
			(buf, list) -> {
				buf.writeVarInt(list.size());
				for (Crest.Instance c : list) c.write(buf);
			},
			buf -> {
				int size = buf.readVarInt();
				List<Crest.Instance> list = new ArrayList<>(size);
				for (int i = 0; i < size; i++) list.add(Crest.Instance.read(buf));
				return list;
			});

	public static final EntityDataAttachment<List<Crest.Instance>> CRESTS =
		regSimple("crests", List::of, true,
			(buf, list) -> {
				buf.writeVarInt(list.size());
				for (Crest.Instance c : list) c.write(buf);
			},
			buf -> {
				int size = buf.readVarInt();
				List<Crest.Instance> list = new ArrayList<>(size);
				for (int i = 0; i < size; i++) list.add(Crest.Instance.read(buf));
				return list;
			});

	public static final EntityDataAttachment<List<Crest.Instance>> OWNED_CRESTS =
		regSimple("owned_crests", List::of, true,
			(buf, list) -> {
				buf.writeVarInt(list.size());
				for (Crest.Instance c : list) c.write(buf);
			},
			buf -> {
				int size = buf.readVarInt();
				List<Crest.Instance> list = new ArrayList<>(size);
				for (int i = 0; i < size; i++) list.add(Crest.Instance.read(buf));
				return list;
			});

	public static final EntityDataAttachment<List<SpecialItemCooldown>> SPECIAL_ITEM_COOLDOWNS =
		regSimple("special_item_cooldowns", List::of, false,
			(buf, list) -> {
				buf.writeVarInt(list.size());
				for (SpecialItemCooldown cd : list) cd.write(buf);
			},
			buf -> {
				int size = buf.readVarInt();
				List<SpecialItemCooldown> list = new ArrayList<>(size);
				for (int i = 0; i < size; i++) list.add(SpecialItemCooldown.read(buf));
				return list;
			});

	public static <T> EntityDataAttachment<T> register(EntityDataAttachment<T> attachment) {
		ATTACHMENTS.add(attachment);
		return attachment;
	}

	private static <T> EntityDataAttachment<T> regSimple(
		String id,
		Supplier<T> def,
		boolean copyOnDeath,
		BiConsumer<FriendlyByteBuf, T> writer,
		Function<FriendlyByteBuf, T> reader
	) {
		return register(ESPlatform.INSTANCE.registerDataAttachment(
			id,
			def,
			copyOnDeath,
			(a, b) -> !Objects.equals(a, b),
			writer,
			reader
		));
	}

	private static EntityDataAttachment<Integer> regInt(String id, int def, boolean copyOnDeath) {
		return regSimple(id, () -> def, copyOnDeath,
			(buf, v) -> buf.writeInt(v),
			buf -> buf.readInt());
	}

	private static EntityDataAttachment<Boolean> regBool(String id, boolean def, boolean copyOnDeath) {
		return regSimple(id, () -> def, copyOnDeath,
			(buf, v) -> buf.writeBoolean(v),
			FriendlyByteBuf::readBoolean);
	}

	private static EntityDataAttachment<Float> regFloat(String id, float def, boolean copyOnDeath) {
		return regSimple(id, () -> def, copyOnDeath,
			(buf, v) -> buf.writeFloat(v),
			FriendlyByteBuf::readFloat);
	}

	private static EntityDataAttachment<String> regString(String id, String def, boolean copyOnDeath) {
		return regSimple(id, () -> def, copyOnDeath,
			(buf, v) -> buf.writeUtf(v),
			FriendlyByteBuf::readUtf);
	}

	private static EntityDataAttachment<Vec3> regVec3(String id, Vec3 def, boolean copyOnDeath) {
		return regSimple(id, () -> def, copyOnDeath,
			(buf, v) -> { buf.writeDouble(v.x); buf.writeDouble(v.y); buf.writeDouble(v.z); },
			buf -> new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
	}

	private static EntityDataAttachment<BlockPos> regBlockPos(String id, boolean copyOnDeath) {
		return regSimple(id, () -> null, copyOnDeath,
			(buf, v) -> { buf.writeInt(v.getX()); buf.writeInt(v.getY()); buf.writeInt(v.getZ()); },
			buf -> new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()));
	}

	private static EntityDataAttachment<ItemStack> regItemStack(String id, boolean copyOnDeath) {
		return regSimple(id, () -> ItemStack.EMPTY, copyOnDeath,
			FriendlyByteBuf::writeItem,
			FriendlyByteBuf::readItem);
	}

	public static List<EntityDataAttachment<?>> getAttachments() {
		return Collections.unmodifiableList(ATTACHMENTS);
	}

	public static EntityDataAttachment<?> byId(ResourceLocation id) {
		return ATTACHMENTS.stream().filter(a -> a.id().equals(id)).findFirst().orElse(null);
	}

	public static void loadClass() {
	}
}
