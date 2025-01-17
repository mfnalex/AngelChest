package de.jeff_media.angelchest.utils;

import com.jeff_media.jefflib.EnumUtils;
import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.enums.CommandAction;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.daddy.Daddy_Stepsister;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class SoundUtils {

    public static void playTpFetchSound(final Player player, final Location location, final CommandAction action) {
        final AngelChestMain main = AngelChestMain.getInstance();
        if (action == CommandAction.FETCH_CHEST) {
            if (!main.getConfig().getBoolean(Config.PLAY_SOUND_ON_FETCH)) {
                return;
            } else {
                if (!Daddy_Stepsister.allows(PremiumFeatures.PLAY_SOUND_ON_TP_OR_FETCH)) {
                    Messages.sendPremiumOnlyConsoleMessage(Config.PLAY_SOUND_ON_FETCH);
                    return;
                }
            }
        }
        if (action == CommandAction.TELEPORT_TO_CHEST) {
            if (!main.getConfig().getBoolean(Config.PLAY_SOUND_ON_TP)) {
                return;
            } else {
                if (!Daddy_Stepsister.allows(PremiumFeatures.PLAY_SOUND_ON_TP_OR_FETCH)) {
                    Messages.sendPremiumOnlyConsoleMessage(Config.PLAY_SOUND_ON_TP);
                    return;
                }
            }
        }

        final Sound sound = EnumUtils.getIfPresent(Sound.class, main.getConfig().getString(Config.SOUND_EFFECT)).orElse(null);

        if (sound == null) {
            main.getLogger().warning("You specified an invalid sound effect: " + main.getConfig().getString(Config.SOUND_EFFECT));
            return;
        }

        final float volume = (float) main.getConfig().getDouble(Config.SOUND_VOLUME);
        final float pitch = (float) main.getConfig().getDouble(Config.SOUND_PITCH);
        final SoundCategory channel = EnumUtils.getIfPresent(SoundCategory.class, main.getConfig().getString(Config.SOUND_CHANNEL)).orElse(SoundCategory.BLOCKS);

        if (player != null && player.isOnline()) {
            player.playSound(location, sound, channel, volume, pitch);
        }

    }

}
