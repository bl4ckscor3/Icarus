package dev.cammiescorner.icarus.core.util;

import dev.cammiescorner.icarus.Icarus;
import dev.cammiescorner.icarus.common.items.WingItem;
import dev.cammiescorner.icarus.core.network.client.DeleteHungerMessage;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class IcarusHelper {
	private static final Tag<Item> FREE_FLIGHT = TagRegistry.item(new Identifier(Icarus.MOD_ID, "free_flight"));

	public static float getAdjustedPitch(Entity entity, float value) {
		var aaa = new Object() {
			float pitch = value;
		};

		if(Icarus.getConfig().canLoopdeloop && entity instanceof PlayerEntity player && player.isFallFlying()) {
			Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);

			component.ifPresent(trinketComponent -> {
				if(trinketComponent.isEquipped(stack -> stack.getItem() instanceof WingItem) || Icarus.HAS_POWERED_FLIGHT.test(entity))
					aaa.pitch = MathHelper.wrapDegrees(entity.getPitch());
			});
		}

		return aaa.pitch;
	}

	public static void applySpeed(PlayerEntity player, Item item) {
		Vec3d rotation = player.getRotationVector();
		Vec3d velocity = player.getVelocity();
		float modifier = Icarus.getConfig().armourSlows ? Math.max(1F, (player.getArmor() / 30F) * Icarus.getConfig().maxSlowedMultiplier) : 1F;
		float speed = (Icarus.getConfig().wingsSpeed * (player.getPitch() < -80 && player.getPitch() > -100 ? 2.75F : 1)) / modifier;

		player.setVelocity(velocity.add(rotation.x * speed + (rotation.x * 1.5D - velocity.x) * speed,
				rotation.y * speed + (rotation.y * 1.5D - velocity.y) * speed,
				rotation.z * speed + (rotation.z * 1.5D - velocity.z) * speed));

		if((item == null || !FREE_FLIGHT.contains(item)) && !player.isCreative())
			DeleteHungerMessage.send();
	}

	public static void stopFlying(PlayerEntity player) {
		if(player.getPitch() < -90 || player.getPitch() > 90) {
			float offset = (player.getPitch() < -90 ? player.getPitch() + 180 : player.getPitch() - 180) * 2;
			player.setPitch((player.getPitch() < -90 ? 180 + offset : -180 - offset) + player.getPitch());
			player.setYaw(180 + player.getYaw());
		}

		player.stopFallFlying();
	}
}
