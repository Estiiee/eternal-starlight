package cn.leolezury.eternalstarlight.common.network;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ESPackets {
	/*
	public static final PacketInfo<SyncAttachmentsPacket> SYNC_ATTACHMENTS = new PacketInfo<>(SyncAttachmentsPacket.TYPE, SyncAttachmentsPacket.STREAM_CODEC, SyncAttachmentsPacket::handle);
	public static final PacketInfo<SimpleActionPacket> NO_PARAMETERS = new PacketInfo<>(SimpleActionPacket.TYPE, SimpleActionPacket.STREAM_CODEC, SimpleActionPacket::handle);
	public static final PacketInfo<ParticlePacket> PARTICLE = new PacketInfo<>(ParticlePacket.TYPE, ParticlePacket.STREAM_CODEC, ParticlePacket::handle);
	public static final PacketInfo<VfxPacket> VFX = new PacketInfo<>(VfxPacket.TYPE, VfxPacket.STREAM_CODEC, VfxPacket::handle);
	public static final PacketInfo<UpdateWeatherPacket> UPDATE_WEATHER = new PacketInfo<>(UpdateWeatherPacket.TYPE, UpdateWeatherPacket.STREAM_CODEC, UpdateWeatherPacket::handle);
	public static final PacketInfo<OpenCrestGuiPacket> OPEN_CREST_GUI = new PacketInfo<>(OpenCrestGuiPacket.TYPE, OpenCrestGuiPacket.STREAM_CODEC, OpenCrestGuiPacket::handle);
	public static final PacketInfo<UpdateCrestsPacket> UPDATE_CRESTS = new PacketInfo<>(UpdateCrestsPacket.TYPE, UpdateCrestsPacket.STREAM_CODEC, UpdateCrestsPacket::handle);
	public static final PacketInfo<UpdateCameraPacket> UPDATE_CAMERA = new PacketInfo<>(UpdateCameraPacket.TYPE, UpdateCameraPacket.STREAM_CODEC, UpdateCameraPacket::handle);
	public static final PacketInfo<ClientMountPacket> CLIENT_MOUNT = new PacketInfo<>(ClientMountPacket.TYPE, ClientMountPacket.STREAM_CODEC, ClientMountPacket::handle);
	public static final PacketInfo<ClientDismountPacket> CLIENT_DISMOUNT = new PacketInfo<>(ClientDismountPacket.TYPE, ClientDismountPacket.STREAM_CODEC, ClientDismountPacket::handle);
	public static final PacketInfo<OpenGatekeeperGuiPacket> OPEN_GATEKEEPER_GUI = new PacketInfo<>(OpenGatekeeperGuiPacket.TYPE, OpenGatekeeperGuiPacket.STREAM_CODEC, OpenGatekeeperGuiPacket::handle);
	public static final PacketInfo<CloseGatekeeperGuiPacket> CLOSE_GATEKEEPER_GUI = new PacketInfo<>(CloseGatekeeperGuiPacket.TYPE, CloseGatekeeperGuiPacket.STREAM_CODEC, CloseGatekeeperGuiPacket::handle);
	public static final PacketInfo<UpdateBookPacket> UPDATE_BOOK = new PacketInfo<>(UpdateBookPacket.TYPE, UpdateBookPacket.STREAM_CODEC, UpdateBookPacket::handle);
	public static final PacketInfo<UpdateBookProgressionPacket> UPDATE_BOOK_PROGRESSION = new PacketInfo<>(UpdateBookProgressionPacket.TYPE, UpdateBookProgressionPacket.STREAM_CODEC, UpdateBookProgressionPacket::handle);
	public static final PacketInfo<OpenBookPacket> OPEN_BOOK = new PacketInfo<>(OpenBookPacket.TYPE, OpenBookPacket.STREAM_CODEC, OpenBookPacket::handle);
	public static final PacketInfo<TriggerEntityEventPacket> TRIGGER_ENTITY_EVENT = new PacketInfo<>(TriggerEntityEventPacket.TYPE, TriggerEntityEventPacket.STREAM_CODEC, TriggerEntityEventPacket::handle);
	public static final PacketInfo<UpdateBossBarPacket> UPDATE_BOSS_BAR = new PacketInfo<>(UpdateBossBarPacket.TYPE, UpdateBossBarPacket.STREAM_CODEC, UpdateBossBarPacket::handle);

	public record PacketInfo<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec, Handler<T> handler) {

	}

	public interface Handler<T extends CustomPacketPayload> {
		void handle(T object, Player player);
	}

	 */

	private static final Map<ResourceLocation, Function<FriendlyByteBuf, ? extends ESPacket>> FACTORY = new HashMap<>();

	public static <T extends ESPacket> void register(ResourceLocation id, Function<FriendlyByteBuf, T> decoder) {
		FACTORY.put(id, decoder);
	}

	public static ESPacket create(ResourceLocation id, FriendlyByteBuf buf) {
		Function<FriendlyByteBuf, ? extends ESPacket> decoder = FACTORY.get(id);
		if (decoder == null) {
			throw new IllegalStateException("No packet registered for id: " + id);
		}
		return decoder.apply(buf);
	}

	public static void init() {
		register(EternalStarlight.id("open_crest_gui"), OpenCrestGuiPacket::read);
		register(EternalStarlight.id("open_gatekeeper_gui"), OpenGatekeeperGuiPacket::read);
		register(EternalStarlight.id("close_gatekeeper_gui"), CloseGatekeeperGuiPacket::read);
		register(EternalStarlight.id("trigger_entity_event"), TriggerEntityEventPacket::read);
		register(EternalStarlight.id("client_mount"), ClientMountPacket::read);
		register(EternalStarlight.id("client_dismount"), ClientDismountPacket::read);
		register(EternalStarlight.id("update_camera"), UpdateCameraPacket::read);
		register(EternalStarlight.id("update_crests"), UpdateCrestsPacket::read);
		register(EternalStarlight.id("update_book"), UpdateBookPacket::read);
		register(EternalStarlight.id("update_book_progression"), UpdateBookProgressionPacket::read);
		register(EternalStarlight.id("open_book"), OpenBookPacket::read);
		register(EternalStarlight.id("particle"), ParticlePacket::read);
		register(EternalStarlight.id("vfx"), VfxPacket::read);
		register(EternalStarlight.id("update_weather"), UpdateWeatherPacket::read);
		register(EternalStarlight.id("sync_attachments"), SyncAttachmentsPacket::read);
		register(EternalStarlight.id("simple_action"), SimpleActionPacket::read);
		register(EternalStarlight.id("update_boss_bar"), UpdateBossBarPacket::read);
	}
}
