package de.jeff_media.angelchest.utils;

import com.google.common.base.Enums;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.enums.CommandAction;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.daddy.Daddy;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class SoundUtils {

    public static void playTpFetchSound(final Player player, final Location location, final CommandAction action) {
        final Main main = Main.getInstance();
        if (action == CommandAction.FETCH_CHEST) {
            if (!main.getConfig().getBoolean(Config.PLAY_SOUND_ON_FETCH)) {
                return;
            } else {
                if (!Daddy.allows(PremiumFeatures.PLAY_SOUND_ON_TP_OR_FETCH)) {
                    Messages.sendPremiumOnlyConsoleMessage(Config.PLAY_SOUND_ON_FETCH);
                    return;
                }
            }
        }
        if (action == CommandAction.TELEPORT_TO_CHEST) {
            if (!main.getConfig().getBoolean(Config.PLAY_SOUND_ON_TP)) {
                return;
            } else {
                if (!Daddy.allows(PremiumFeatures.PLAY_SOUND_ON_TP_OR_FETCH)) {
                    Messages.sendPremiumOnlyConsoleMessage(Config.PLAY_SOUND_ON_TP);
                    return;
                }
            }
        }

        final Sound sound = Enums.getIfPresent(Sound.class, main.getConfig().getString(Config.SOUND_EFFECT)).orNull();

        if (sound == null) {
            main.getLogger().warning("You specified an invalid sound effect: " + main.getConfig().getString(Config.SOUND_EFFECT));
            return;
        }

        final float volume = (float) main.getConfig().getDouble(Config.SOUND_VOLUME);
        final float pitch = (float) main.getConfig().getDouble(Config.SOUND_PITCH);
        final SoundCategory channel = Enums.getIfPresent(SoundCategory.class, main.getConfig().getString(Config.SOUND_CHANNEL)).or(SoundCategory.BLOCKS);

        if (player != null && player.isOnline()) {
            player.playSound(location, sound, channel, volume, pitch);
        }

    }

}
