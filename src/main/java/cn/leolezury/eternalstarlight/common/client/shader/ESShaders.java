package cn.leolezury.eternalstarlight.common.client.shader;

import cn.leolezury.eternalstarlight.common.config.ESConfig;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ESShaders {
	private static ShaderInstance crestSelectionGui;
	private static ShaderInstance renderTypeStarlightPortal;
	private static ShaderInstance renderTypeEclipse;
	private static ShaderInstance aurora;

	public static ShaderInstance getCrestSelectionGui() {
		return crestSelectionGui;
	}

	public static void setCrestSelectionGui(ShaderInstance crestSelectionGui) {
		ESShaders.crestSelectionGui = crestSelectionGui;
	}

	public static ShaderInstance getRenderTypeStarlightPortal() {
		return renderTypeStarlightPortal;
	}

	public static void setRenderTypeStarlightPortal(ShaderInstance renderTypeStarlightPortal) {
		ESShaders.renderTypeStarlightPortal = renderTypeStarlightPortal;
	}

	public static ShaderInstance getRenderTypeEclipse() {
		return renderTypeEclipse;
	}

	public static void setRenderTypeEclipse(ShaderInstance renderTypeEclipse) {
		ESShaders.renderTypeEclipse = renderTypeEclipse;
	}

	public static ShaderInstance getAurora() {
		return aurora;
	}

	public static void setAurora(ShaderInstance aurora) {
		ESShaders.aurora = aurora;
	}

	public static class AuroraColors {
		private static final Vec3 DEFAULT_A = new Vec3(0.0, 0.5, 1.0);
		private static final Vec3 DEFAULT_B = new Vec3(0.0, 1.0, 0.5);

		public static Vec3 currentA = DEFAULT_A;
		public static Vec3 currentB = DEFAULT_B;

		private static List<? extends String> lastList;
		private static Map<ResourceLocation, Vec3[]> colorMap = Map.of();

		public static void update(Level level, BlockPos pos) {
			reloadIfChanged();

			Vec3 targetA = DEFAULT_A, targetB = DEFAULT_B;
			ResourceLocation id = level.getBiome(pos).unwrapKey().map(ResourceKey::location).orElse(null);
			if (id != null) {
				Vec3[] pair = colorMap.get(id);
				if (pair != null) {
					targetA = pair[0];
					targetB = pair[1];
				}
			}
			currentA = approach(currentA, targetA);
			currentB = approach(currentB, targetB);
		}

		private static void reloadIfChanged() {
			List<? extends String> list = ESConfig.auroraBiomeColors.get();
			if (list == lastList) return;
			lastList = list;
			Map<ResourceLocation, Vec3[]> map = new HashMap<>();
			for (String s : list) {
				Map.Entry<ResourceLocation, Vec3[]> e = parseEntry(s);
				if (e != null) map.put(e.getKey(), e.getValue());
			}
			colorMap = map;
		}

		public static Map.Entry<ResourceLocation, Vec3[]> parseEntry(String s) {
			try {
				String[] halves = s.split("=", 2);
				String[] colors = halves[1].split(",", 2);
				ResourceLocation id = ResourceLocation.tryParse(halves[0].trim());
				Vec3 a = parseColor(colors[0].trim());
				Vec3 b = parseColor(colors[1].trim());
				if (id == null || a == null || b == null) return null;
				return Map.entry(id, new Vec3[]{a, b});
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
		}

		private static Vec3 parseColor(String s) {
			String h = s.startsWith("#") ? s.substring(1) : s;
			if (h.length() != 6) return null;
			try {
				return new Vec3(
					Integer.parseInt(h.substring(0, 2), 16) / 255.0,
					Integer.parseInt(h.substring(2, 4), 16) / 255.0,
					Integer.parseInt(h.substring(4, 6), 16) / 255.0);
			} catch (NumberFormatException e) {
				return null;
			}
		}

		private static Vec3 approach(Vec3 cur, Vec3 target) {
			if (cur.distanceToSqr(target) < 1.0E-5) return target;
			return cur.lerp(target, 0.02);
		}
	}
}
