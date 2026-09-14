package cn.leolezury.eternalstarlight.common.platform;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForgeEntityDataAttachment<T> implements EntityDataAttachment<T> {

	private final ResourceLocation id;
	private final Supplier<T> defaultValue;
	private final boolean copyOnDeath;
	private final BiPredicate<T, T> shouldSyncPredicate;
	private final BiConsumer<FriendlyByteBuf, T> writer;
	private final Function<FriendlyByteBuf, T> reader;

	private final Map<Integer, T> values = new ConcurrentHashMap<>();

	public ForgeEntityDataAttachment(
		ResourceLocation id,
		Supplier<T> defaultValue,
		boolean copyOnDeath,
		BiPredicate<T, T> shouldSync,
		BiConsumer<FriendlyByteBuf, T> writer,
		Function<FriendlyByteBuf, T> reader
	) {
		this.id = id;
		this.defaultValue = defaultValue;
		this.copyOnDeath = copyOnDeath;
		this.shouldSyncPredicate = shouldSync;
		this.writer = writer;
		this.reader = reader;
	}

	@Override
	public ResourceLocation id() {
		return id;
	}

	@Override
	public boolean hasData(Entity entity) {
		return values.containsKey(entity.getId());
	}

	@Override
	public T getData(Entity entity) {
		return values.computeIfAbsent(entity.getId(), k -> defaultValue.get());
	}

	@Override
	public Optional<T> getExistingData(Entity entity) {
		return Optional.ofNullable(values.get(entity.getId()));
	}

	@Override
	public @Nullable T setDataUnsynced(Entity entity, T data) {
		int id = entity.getId();
		T prev = values.get(id);
		values.put(id, data);
		return prev;
	}

	@Override
	public @Nullable T removeDataUnsynced(Entity entity) {
		return values.remove(entity.getId());
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf buf, @Nullable T value) {
		if (value != null) {
			writer.accept(buf, value);
		}
	}

	@Override
	public @Nullable T readFromNetwork(FriendlyByteBuf buf) {
		return reader.apply(buf);
	}

	@Override
	public boolean shouldSync(@Nullable T v1, @Nullable T v2) {
		return shouldSyncPredicate.test(v1, v2);
	}

	public boolean copyOnDeath() {
		return copyOnDeath;
	}
}
