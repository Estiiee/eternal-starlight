package cn.leolezury.eternalstarlight.common.network;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.util.ESMiscUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class UpdateBossBarPacket implements ESPacket {
	private final UUID barId;
	private final int barType;

	public UpdateBossBarPacket(UUID barId, int barType) {
		this.barId = barId;
		this.barType = barType;
	}

	public static UpdateBossBarPacket read(FriendlyByteBuf buf) {
		UUID barId = buf.readUUID();
		int barType = buf.readVarInt();
		return new UpdateBossBarPacket(barId, barType);
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeUUID(barId);
		buf.writeVarInt(barType);
	}

	@Override
	public void handle(Player player) {
		ESMiscUtil.runWhenOnClient(() ->
			() -> EternalStarlight.getClientHelper().handleUpdateBossBar(this)
		);
	}

	@Override
	public ResourceLocation id() {
		return EternalStarlight.id("update_boss_bar");
	}

	public UUID barId() {
		return barId;
	}

	public int barType() {
		return barType;
	}
}