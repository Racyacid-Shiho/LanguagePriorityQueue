package cn.racyacid.lpq.mixin;

import cn.racyacid.lpq.LanguagePriorityQueue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(LanguageManager.class)
public abstract class LanguageManagerMixin {
	@Shadow private String currentLanguageCode;

	@Inject(method = "reload", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
	private void insertQueue(ResourceManager manager, CallbackInfo ci, @Local List<String> list) {
		Map<String, String[]> queues = LanguagePriorityQueue.getQueues();
		if (!queues.containsKey(currentLanguageCode)) return;

		list.addAll(List.of(queues.get(currentLanguageCode)));
	}

	@Inject(method = "reload", at = @At("TAIL"))
	private void logLoadedQueue(ResourceManager manager, CallbackInfo ci, @Local List<String> list) {
		LanguagePriorityQueue.LOGGER.info("Loaded language priority queue(In reverse order): {}", list);
	}
}