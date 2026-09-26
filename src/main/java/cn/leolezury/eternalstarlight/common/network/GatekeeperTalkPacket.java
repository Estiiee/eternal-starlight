package cn.leolezury.eternalstarlight.common.network;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.entity.living.boss.gatekeeper.TheGatekeeper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class GatekeeperTalkPacket implements ESPacket {
	private final int id;

	public GatekeeperTalkPacket(int id) {
		this.id = id;
	}

	public static GatekeeperTalkPacket read(FriendlyByteBuf buf) {
		return new GatekeeperTalkPacket(buf.readInt());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeInt(id);
	}

	@Override
	public void handle(Player player) {
		Entity entity = player.level().getEntity(id);
		if (entity != null && entity instanceof TheGatekeeper gatekeeper) {
			player.level().broadcastEntityEvent(gatekeeper, TheGatekeeper.EVENT_TALK);
		}
	}

	@Override
	public ResourceLocation id() {
		return EternalStarlight.id("gatekeeper_talk");
	}

	public int entityId() { return id; }
}
