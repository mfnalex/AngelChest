package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.Main;
import de.jeff_media.daddy.Daddy;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class DiscordVerificationUtils {
    private static String addLineBreaks(final String code) {
        final int maxLength = 80;
        int lengthUntilBegin = 19;
        String rest = code;
        final List<String> lines = new ArrayList<>();

        while (rest.length() > maxLength - lengthUntilBegin) {
            lines.add(rest.substring(0, maxLength - lengthUntilBegin));
            rest = rest.substring(maxLength - lengthUntilBegin);
            lengthUntilBegin = 0;
        }
        if (!rest.isEmpty()) {
            lines.add(rest);
        }
        final String[] linesArray = lines.toArray(new String[0]);
        return StringUtils.join(linesArray, "\n");
    }

    public static void createVerificationFile() {

        final Main main = Main.getInstance();

        final File verifyFile = new File(main.getDataFolder(), "discord-verification.html");
        if (!verifyFile.exists()) {
            for (final String line : new String[] {"§b=====================================================", "§b                 Discord Verification", "§b", "§bThank you for buying AngelChestPlus. To get exclusive", "§bDiscord support, you have to verify your copy of", "§bAngelChestPlus. Just take a look at the following file:", "§b", "§b" + verifyFile.getAbsolutePath(), "§b====================================================="}) {
                main.getLogger().warning(line);
            }
            main.saveResource("discord-verification.html", false);
            @SuppressWarnings("UnusedAssignment") String verifyCode = "QnV5IHRoZSBwbHVnaW4gaWYgeW91IGxpa2UgaXQuLi4gaHR0cHM6Ly93d3cuc3BpZ290bWMub3JnL3Jlc291cmNlcy9hbmdlbGNoZXN0cGx1cy44ODIxNA==";
            /*Daddy start*/
            verifyCode = Daddy.getDiscordVerificationCode();
            /*Daddy end*/
            try {
                FileUtils.replaceStringsInFile(verifyFile, "{verificationCode}", addLineBreaks(verifyCode));
            } catch (final IOException ioException) {
                main.getLogger().severe("Could not update discord-verification.html");
            }
        }
    }
}
