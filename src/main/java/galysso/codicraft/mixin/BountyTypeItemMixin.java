package galysso.codicraft.mixin;

import com.glisco.numismaticoverhaul.ModComponents;
import io.ejekta.bountiful.bounty.BountyDataEntry;
import io.ejekta.bountiful.bounty.types.builtin.BountyTypeItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static wraith.fwaystones.util.Utils.getTeleportCostItem;

@Mixin(BountyTypeItem.class)
public abstract class BountyTypeItemMixin {
    private static final Identifier copperId = new Identifier("numismatic-overhaul", "copper_coin");
    private static final Identifier silverId = new Identifier("numismatic-overhaul", "silver_coin");
    private static final Identifier goldId = new Identifier("numismatic-overhaul", "gold_coin");

    @Unique
    private static boolean useNumismaticCost(Identifier id) {
        return id.equals(copperId) || id.equals(silverId) || id.equals(goldId);
    }

    @Unique
    private static int getNumismaticFactor(Identifier id) {
        return id.equals(copperId) ? 1 : id.equals(silverId) ? 100 : 10000;
    }

    @Inject(method = "giveReward", at = @At("HEAD"), cancellable = true)
    private void injectModifyReward(BountyDataEntry entry, PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        Item item = BountyTypeItem.Companion.getItem(entry);
        Identifier id = Registries.ITEM.getId(item);
        if (useNumismaticCost(id)) {
            long rewardValue = (long) getNumismaticFactor(id) * entry.getAmount();
            ModComponents.CURRENCY.get(player).modify(rewardValue); // Cleaner invocation
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
