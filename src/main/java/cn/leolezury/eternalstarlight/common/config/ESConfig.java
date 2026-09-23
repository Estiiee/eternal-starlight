package cn.leolezury.eternalstarlight.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ESConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	// ===== GENERAL =====
	public static ForgeConfigSpec.BooleanValue enableDataFixer;
	public static ForgeConfigSpec.BooleanValue enablePortalShader;
	public static ForgeConfigSpec.BooleanValue enableScreenShake;
	public static ForgeConfigSpec.BooleanValue enableLootChest;
	public static ForgeConfigSpec.BooleanValue enableBossRespawn;
	public static ForgeConfigSpec.BooleanValue spawnInEternalStarlight;
	public static ForgeConfigSpec.BooleanValue respawnInEternalStarlight;
	public static ForgeConfigSpec.BooleanValue startWithGuidebook;
	public static ForgeConfigSpec.BooleanValue aethersentMeteorReplaceBlocks;
	public static ForgeConfigSpec.BooleanValue blockUnknownAttachmentWarning;

	public static ForgeConfigSpec.DoubleValue aethersentMeteorDropRate;

	public static ForgeConfigSpec.IntValue mobMaxTearyTicks;
	public static ForgeConfigSpec.IntValue bossRespawnCooldown;

	// ===== ITEMS =====
	public static ForgeConfigSpec.DoubleValue playerAethersentMeteorDamageScale;

	public static class ChainOfSoulsEntry {
		public final ForgeConfigSpec.DoubleValue maxRange;
		public final ForgeConfigSpec.DoubleValue soulAbsorbDamage;
		public final ForgeConfigSpec.DoubleValue healPercentage;

		public ChainOfSoulsEntry(ForgeConfigSpec.Builder b) {
			b.push("chainOfSouls");
			maxRange = b.defineInRange("maxRange", 64.0, 0.0, 10000.0);
			soulAbsorbDamage = b.defineInRange("soulAbsorbDamage", 2.0, 0.0, 10000.0);
			healPercentage = b.defineInRange("healPercentage", 0.5, 0.0, 100.0);
			b.pop();
		}
	}

	public static class CrystalbornCatalystEntry {
		public final ForgeConfigSpec.IntValue maxRange;
		public final ForgeConfigSpec.IntValue energyPerShard;

		public CrystalbornCatalystEntry(ForgeConfigSpec.Builder b) {
			b.push("crystalbornCatalyst");
			maxRange = b.defineInRange("maxRange", 128, 0, 100000);
			energyPerShard = b.defineInRange("energyPerShard", 50, 0, 100000);
			b.pop();
		}
	}

	public static class AlloyFurnaceEntry {
		public final ForgeConfigSpec.IntValue totalOverheatTicks;
		public final ForgeConfigSpec.IntValue explosionRadius;

		public AlloyFurnaceEntry(ForgeConfigSpec.Builder b) {
			b.push("alloyFurnace");
			totalOverheatTicks = b.defineInRange("totalOverheatTicks", 12000, 0, 1000000);
			explosionRadius = b.defineInRange("explosionRadius", 3, 0, 1000);
			b.pop();
		}
	}

	public static ChainOfSoulsEntry chainOfSouls;
	public static CrystalbornCatalystEntry crystalbornCatalyst;
	public static AlloyFurnaceEntry alloyFurnace;

	// ===== MOB ENTRY TYPES =====
	public static class MobEntry {
		public final ForgeConfigSpec.DoubleValue maxHealth;
		public final ForgeConfigSpec.DoubleValue armor;
		public final ForgeConfigSpec.BooleanValue canSpawn;

		public MobEntry(ForgeConfigSpec.Builder b, String name, double hp, double armorVal, boolean spawn) {
			b.push(name);
			maxHealth = b.defineInRange("maxHealth", hp, 0.0, 10000.0);
			armor = b.defineInRange("armor", armorVal, 0.0, 10000.0);
			canSpawn = b.define("canSpawn", spawn);
			b.pop();
		}
	}

	public static class AttackingMobEntry {
		public final ForgeConfigSpec.DoubleValue maxHealth;
		public final ForgeConfigSpec.DoubleValue armor;
		public final ForgeConfigSpec.DoubleValue attackDamage;
		public final ForgeConfigSpec.DoubleValue followRange;
		public final ForgeConfigSpec.BooleanValue canSpawn;

		public AttackingMobEntry(ForgeConfigSpec.Builder b, String name, double hp, double armorVal, double dmg, double range, boolean spawn) {
			b.push(name);
			maxHealth = b.defineInRange("maxHealth", hp, 0.0, 10000.0);
			armor = b.defineInRange("armor", armorVal, 0.0, 10000.0);
			attackDamage = b.defineInRange("attackDamage", dmg, 0.0, 10000.0);
			followRange = b.defineInRange("followRange", range, 0.0, 10000.0);
			canSpawn = b.define("canSpawn", spawn);
			b.pop();
		}
	}

	public static class BossEntry {
		public final ForgeConfigSpec.DoubleValue maxHealth;
		public final ForgeConfigSpec.DoubleValue armor;
		public final ForgeConfigSpec.DoubleValue attackDamageScale;
		public final ForgeConfigSpec.DoubleValue followRange;
		public final ForgeConfigSpec.BooleanValue canSpawn;

		public BossEntry(ForgeConfigSpec.Builder b, String name, double hp, double armorVal, double scale, double range, boolean spawn) {
			b.push(name);
			maxHealth = b.defineInRange("maxHealth", hp, 0.0, 10000.0);
			armor = b.defineInRange("armor", armorVal, 0.0, 10000.0);
			attackDamageScale = b.defineInRange("attackDamageScale", scale, 0.0, 10000.0);
			followRange = b.defineInRange("followRange", range, 0.0, 10000.0);
			canSpawn = b.define("canSpawn", spawn);
			b.pop();
		}
	}

	public static class GatekeeperEntry {
		public final ForgeConfigSpec.DoubleValue maxHealth;
		public final ForgeConfigSpec.DoubleValue armor;
		public final ForgeConfigSpec.DoubleValue attackDamage;
		public final ForgeConfigSpec.DoubleValue followRange;
		public final ForgeConfigSpec.BooleanValue canSpawn;
		public final ForgeConfigSpec.BooleanValue canAlwaysHurtWhenFighting;

		public GatekeeperEntry(ForgeConfigSpec.Builder b, String name, double hp, double armorVal, double dmg, double range, boolean spawn, boolean alwaysHurt) {
			b.push(name);
			maxHealth = b.defineInRange("maxHealth", hp, 0.0, 10000.0);
			armor = b.defineInRange("armor", armorVal, 0.0, 10000.0);
			attackDamage = b.defineInRange("attackDamage", dmg, 0.0, 10000.0);
			followRange = b.defineInRange("followRange", range, 0.0, 10000.0);
			canSpawn = b.define("canSpawn", spawn);
			canAlwaysHurtWhenFighting = b.define("canAlwaysHurtWhenFighting", alwaysHurt);
			b.pop();
		}
	}

	public static class StranghoulEntry {
		public final ForgeConfigSpec.DoubleValue maxHealth;
		public final ForgeConfigSpec.DoubleValue armor;
		public final ForgeConfigSpec.DoubleValue attackDamage;
		public final ForgeConfigSpec.DoubleValue followRange;
		public final ForgeConfigSpec.BooleanValue canSpawn;
		public final ForgeConfigSpec.IntValue hiringCooldown;

		public StranghoulEntry(ForgeConfigSpec.Builder b, String name, double hp, double armorVal, double dmg, double range, boolean spawn, int cooldown) {
			b.push(name);
			maxHealth = b.defineInRange("maxHealth", hp, 0.0, 10000.0);
			armor = b.defineInRange("armor", armorVal, 0.0, 10000.0);
			attackDamage = b.defineInRange("attackDamage", dmg, 0.0, 10000.0);
			followRange = b.defineInRange("followRange", range, 0.0, 10000.0);
			canSpawn = b.define("canSpawn", spawn);
			hiringCooldown = b.defineInRange("hiringCooldown", cooldown, 0, Integer.MAX_VALUE);
			b.pop();
		}
	}

	public static class CreteorEntry {
		public final ForgeConfigSpec.DoubleValue maxHealth;
		public final ForgeConfigSpec.DoubleValue armor;
		public final ForgeConfigSpec.DoubleValue attackDamage;
		public final ForgeConfigSpec.DoubleValue followRange;
		public final ForgeConfigSpec.BooleanValue canSpawn;
		public final ForgeConfigSpec.DoubleValue spawnChance;

		public CreteorEntry(ForgeConfigSpec.Builder b, String name, double hp, double armorVal, double dmg, double range, boolean spawn, double chance) {
			b.push(name);
			maxHealth = b.defineInRange("maxHealth", hp, 0.0, 10000.0);
			armor = b.defineInRange("armor", armorVal, 0.0, 10000.0);
			attackDamage = b.defineInRange("attackDamage", dmg, 0.0, 10000.0);
			followRange = b.defineInRange("followRange", range, 0.0, 10000.0);
			canSpawn = b.define("canSpawn", spawn);
			spawnChance = b.defineInRange("spawnChance", chance, 0.0, 1.0);
			b.pop();
		}
	}

	// ===== MOB CONFIG ENTRIES =====
	public static MobEntry boarwarf;
	public static AttackingMobEntry astralGolem;
	public static AttackingMobEntry gleech;
	public static AttackingMobEntry lonestarSkeleton;
	public static AttackingMobEntry nightfallSpider;
	public static AttackingMobEntry seeker;
	public static AttackingMobEntry thirstWalker;
	public static CreteorEntry creteor;
	public static AttackingMobEntry tinyCreteor;
	public static StranghoulEntry stranghoul;
	public static MobEntry ent;
	public static MobEntry ratlin;
	public static AttackingMobEntry zombifiedRatlin;
	public static MobEntry shadowSnail;
	public static MobEntry yeti;
	public static AttackingMobEntry auroraDeer;
	public static AttackingMobEntry crystallizedMoth;
	public static MobEntry shimmerLacewing;
	public static MobEntry starfireBird;
	public static MobEntry grimstoneGolem;
	public static MobEntry aethersentGolem;
	public static AttackingMobEntry luminofish;
	public static AttackingMobEntry luminaris;
	public static AttackingMobEntry twilightGaze;
	public static GatekeeperEntry theGatekeeper;
	public static BossEntry starlightGolem;
	public static AttackingMobEntry freeze;
	public static BossEntry permafrost;
	public static BossEntry lunarMonstrosity;
	public static AttackingMobEntry tangled;
	public static AttackingMobEntry tangledSkull;
	public static BossEntry solarCreeper;

	static {
		// ===== GENERAL =====
		BUILDER.push("general");
		enableDataFixer = BUILDER.define("enableDataFixer", false);
		enablePortalShader = BUILDER.define("enablePortalShader", true);
		enableScreenShake = BUILDER.define("enableScreenShake", true);
		enableLootChest = BUILDER.define("enableLootChest", true);
		enableBossRespawn = BUILDER.define("enableBossRespawn", true);
		bossRespawnCooldown = BUILDER.defineInRange("bossRespawnCooldown", 36000, 0, Integer.MAX_VALUE);
		spawnInEternalStarlight = BUILDER.define("spawnInEternalStarlight", false);
		respawnInEternalStarlight = BUILDER.define("respawnInEternalStarlight", false);
		startWithGuidebook = BUILDER.define("startWithGuidebook", false);
		aethersentMeteorDropRate = BUILDER.defineInRange("aethersentMeteorDropRate", 0.0001, 0.0, 1.0);
		aethersentMeteorReplaceBlocks = BUILDER.define("aethersentMeteorReplaceBlocks", false);
		mobMaxTearyTicks = BUILDER.defineInRange("mobMaxTearyTicks", 200, 0, 100000);
		blockUnknownAttachmentWarning = BUILDER.define("blockUnknownAttachmentWarning", true);
		BUILDER.pop();

		// ===== ITEMS =====
		BUILDER.push("items");
		playerAethersentMeteorDamageScale = BUILDER.defineInRange("playerAethersentMeteorDamageScale", 1.0, 0.0, 10000.0);
		chainOfSouls = new ChainOfSoulsEntry(BUILDER);
		crystalbornCatalyst = new CrystalbornCatalystEntry(BUILDER);
		alloyFurnace = new AlloyFurnaceEntry(BUILDER);
		BUILDER.pop();

		// ===== MOBS =====
		BUILDER.push("mobs");

		boarwarf = new MobEntry(BUILDER, "boarwarf", 30, 10, true);
		astralGolem = new AttackingMobEntry(BUILDER, "astralGolem", 100, 10, 10, 100, true);
		gleech = new AttackingMobEntry(BUILDER, "gleech", 8, 0, 1, 16, true);
		lonestarSkeleton = new AttackingMobEntry(BUILDER, "lonestarSkeleton", 20, 0, 3.2, 16, true);
		nightfallSpider = new AttackingMobEntry(BUILDER, "nightfallSpider", 10, 0, 2, 16, true);
		seeker = new AttackingMobEntry(BUILDER, "seeker", 15, 0, 3, 16, true);
		thirstWalker = new AttackingMobEntry(BUILDER, "thirstWalker", 40, 0, 4.5, 32, true);
		creteor = new CreteorEntry(BUILDER, "creteor", 15, 0, 5, 48, true, 0.7);
		tinyCreteor = new AttackingMobEntry(BUILDER, "tinyCreteor", 5, 0, 2, 48, true);
		stranghoul = new StranghoulEntry(BUILDER, "stranghoul", 30, 2, 2, 32, true, 8000);
		ent = new MobEntry(BUILDER, "ent", 10, 0, true);
		ratlin = new MobEntry(BUILDER, "ratlin", 15, 0, true);
		zombifiedRatlin = new AttackingMobEntry(BUILDER, "zombifiedRatlin", 20, 2, 3, 35, true);
		shadowSnail = new MobEntry(BUILDER, "shadowSnail", 8, 6, true);
		yeti = new MobEntry(BUILDER, "yeti", 20, 0, true);
		auroraDeer = new AttackingMobEntry(BUILDER, "auroraDeer", 20, 0, 3, 16, true);
		crystallizedMoth = new AttackingMobEntry(BUILDER, "crystallizedMoth", 20, 0, 1.5, 64, true);
		shimmerLacewing = new MobEntry(BUILDER, "shimmerLacewing", 5, 0, true);
		starfireBird = new MobEntry(BUILDER, "starfireBird", 15, 0, true);
		grimstoneGolem = new MobEntry(BUILDER, "grimstoneGolem", 20, 0, true);
		aethersentGolem = new MobEntry(BUILDER, "aethersentGolem", 40, 10, true);
		luminofish = new AttackingMobEntry(BUILDER, "luminofish", 3, 0, 3, 16, true);
		luminaris = new AttackingMobEntry(BUILDER, "luminaris", 3, 0, 3, 64, true);
		twilightGaze = new AttackingMobEntry(BUILDER, "twilightGaze", 10, 0, 3, 16, true);
		theGatekeeper = new GatekeeperEntry(BUILDER, "theGatekeeper", 175, 15, 1, 200, true, false);
		starlightGolem = new BossEntry(BUILDER, "starlightGolem", 200, 10, 1, 200, true);
		freeze = new AttackingMobEntry(BUILDER, "freeze", 15, 0, 3, 32, true);
		permafrost = new BossEntry(BUILDER, "permafrost", 120, 10, 1, 100, true);
		lunarMonstrosity = new BossEntry(BUILDER, "lunarMonstrosity", 200, 12, 1, 200, true);
		tangled = new AttackingMobEntry(BUILDER, "tangled", 20, 0, 5, 64, true);
		tangledSkull = new AttackingMobEntry(BUILDER, "tangledSkull", 1, 0, 3, 64, true);
		solarCreeper = new BossEntry(BUILDER, "solarCreeper", 250, 10, 1, 200, true);

		BUILDER.pop();

		SPEC = BUILDER.build();
	}
}
