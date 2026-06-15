package com.vehector.nextstaffchat.paper.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Multi-format colour parser. Accepts:
 *   - Legacy & codes  (&a, &b, &l, ...)
 *   - Hex            (&#FFAA00)
 *   - MiniMessage    (<red>, <gradient:red:blue>, <bold>, ...)
 */
public final class ColorUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_AMP =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexCharacter('#')
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();
    private static final Pattern LEGACY_HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern AMP_LEGACY = Pattern.compile("(?i)&([0-9A-FK-OR])");

    private ColorUtil() {}

    /**
     * Parse a string with any of the supported colour formats into an Adventure Component.
     * Legacy &-codes are converted to MiniMessage tags first, so the final pipeline is MiniMessage.
     */
    public static Component parse(String input) {
        if (input == null || input.isEmpty()) return Component.empty();
        String s = input;

        // Convert &#RRGGBB -> <#RRGGBB>
        Matcher hm = LEGACY_HEX.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (hm.find()) hm.appendReplacement(sb, "<#" + hm.group(1) + ">");
        hm.appendTail(sb);
        s = sb.toString();

        // Convert legacy &-codes to MiniMessage equivalents.
        s = AMP_LEGACY.matcher(s).replaceAll(m -> legacyToMini(m.group(1).toLowerCase()));

        try {
            return MINI.deserialize(s);
        } catch (Exception e) {
            // Fallback to plain legacy parser if MiniMessage chokes.
            return LEGACY_AMP.deserialize(input);
        }
    }

    public static String strip(String input) {
        return PlainTextComponentSerializer.plainText().serialize(parse(input));
    }

    private static String legacyToMini(String code) {
        return switch (code) {
            case "0" -> "<black>";
            case "1" -> "<dark_blue>";
            case "2" -> "<dark_green>";
            case "3" -> "<dark_aqua>";
            case "4" -> "<dark_red>";
            case "5" -> "<dark_purple>";
            case "6" -> "<gold>";
            case "7" -> "<gray>";
            case "8" -> "<dark_gray>";
            case "9" -> "<blue>";
            case "a" -> "<green>";
            case "b" -> "<aqua>";
            case "c" -> "<red>";
            case "d" -> "<light_purple>";
            case "e" -> "<yellow>";
            case "f" -> "<white>";
            case "k" -> "<obfuscated>";
            case "l" -> "<bold>";
            case "m" -> "<strikethrough>";
            case "n" -> "<underlined>";
            case "o" -> "<italic>";
            case "r" -> "<reset>";
            default  -> "";
        };
    }
}
