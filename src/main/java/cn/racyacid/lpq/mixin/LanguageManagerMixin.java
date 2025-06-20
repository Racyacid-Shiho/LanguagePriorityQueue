package cn.racyacid.lpq.mixin;

import cn.racyacid.lpq.LanguagePriorityQueue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(LanguageManager.class)
public abstract class LanguageManagerMixin {
	@Shadow private String currentCode;

	@Inject(method = "onResourceManagerReload", at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z", shift = At.Shift.AFTER))
	private void insertQueue(ResourceManager resourceManager, CallbackInfo ci, @Local List<String> list) {
		Map<String, String[]> queues = LanguagePriorityQueue.getQueues();
		if (!queues.containsKey(currentCode)) return;

		list.addAll(List.of(queues.get(currentCode)));
	}

	@Inject(method = "onResourceManagerReload", at = @At("TAIL"))
	private void logLoadedQueue(ResourceManager manager, CallbackInfo ci, @Local List<String> list) {
		LanguagePriorityQueue.LOGGER.info(String.format("Language Loading Order: %s(In reverse order)", list));
	}
}