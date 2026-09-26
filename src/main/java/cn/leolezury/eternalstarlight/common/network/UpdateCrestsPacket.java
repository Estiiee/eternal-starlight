package cn.leolezury.eternalstarlight.common.network;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.crest.Crest;
import cn.leolezury.eternalstarlight.common.util.ESCrestUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class UpdateCrestsPacket implements ESPacket {

	private final List<Crest.Instance> crests;

	public UpdateCrestsPacket(List<Crest.Instance> crests) {
		this.crests = crests;
	}

	public static UpdateCrestsPacket read(FriendlyByteBuf buf) {
		int size = buf.readVarInt();
		List<Crest.Instance> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			list.add(Crest.Instance.read(buf));
		}
		return new UpdateCrestsPacket(list);
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(crests.size());
		for (Crest.Instance inst : crests) {
			inst.write(buf);
		}
	}

	@Override
	public void handle(Player player) {
		if (!player.level().isClientSide) {
			List<Crest.Instance> crestList = crests;
			List<Crest.Instance> owned = ESCrestUtil.getOwnedCrests(player);

			// Validate: level must be in range, and player must own that crest at >= the requested level
			boolean invalid = crestList.stream().anyMatch(c -> {
				int level = c.level();
				if (level < 1 || level > c.crest().value().maxLevel()) {
					return true;
				}
				return owned.stream()
					.filter(o -> o.crest().value() == c.crest().value())
					.noneMatch(o -> level <= o.level());
			});

			if (!invalid) {
				ESCrestUtil.setCrests(player, crestList);
			}
		}
	}

	@Override
	public ResourceLocation id() {
		return EternalStarlight.id("update_crests");
	}

	public List<Crest.Instance> crests() {
		return crests;
	}
}
