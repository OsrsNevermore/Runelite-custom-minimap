package com.orbhidermini;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("Orbhidermini")
public interface OrbHiderminiConfig extends Config
{
    @ConfigItem(
            keyName = "hideXpOrb",
            name = "Hide XP orb",
            description = "Configures whether the XP orb is hidden",
            position = 0
    )
    default boolean hideXpOrb()
    {
        return true;
    }

    @ConfigItem(
            keyName = "hideHealthOrb",
            name = "Hide Hitpoints orb",
            description = "Configures whether the Hitpoints orb is hidden",
            position = 1
    )
    default boolean hideHealthOrb()
    {
        return false;
    }

    @ConfigItem(
            keyName = "hidePrayerOrb",
            name = "Hide Prayer orb",
            description = "Configures whether the Prayer orb is hidden",
            position = 2
    )
    default boolean hidePrayerOrb()
    {
        return false;
    }

    @ConfigItem(
            keyName = "hideRunOrb",
            name = "Hide Run orb",
            description = "Configures whether the Run orb is hidden",
            position = 3
    )
    default boolean hideRunOrb()
    {
        return false;
    }

    @ConfigItem(
            keyName = "hideSpecOrb",
            name = "Hide Special Attack orb",
            description = "Hides the Special Attack Orb.",
            position = 4
    )
    default boolean hideSpecOrb()
    {
        return true;
    }

    @ConfigItem(
            keyName = "hideWorldMapOrb",
            name = "Hide World Map orb",
            description = "Hides the World Map Orb.",
            position = 5
    )
    default boolean hideWorldMapOrb()
    {
        return true;
    }

    @ConfigItem(
            keyName = "hideLogoutButton",
            name = "Hide logout button (X)",
            description = "Hides the logout button.",
            position = 6
    )
    default boolean hideLogoutButton()
    {
        return true;
    }

    @ConfigItem(
            keyName = "disableHUDClicking",
            name = "Disable Compass/Minimap Clicking",
            description = "Allows clicking through Compass/Map.<br>" + "COMPASS/MAP WILL CONTINUE TO BLOCK CLICKING IF THEY ARE DISABLED BUT THIS IS NOT USED.",
            position = 7
    )
    default boolean disableHUDClicking()
    {
        return false;
    }

    @ConfigItem(
            keyName = "hideCompass",
            name = "Hide Compass",
            description = "Hides the compass orb.",
            position = 8
    )
    default boolean hideCompass() { return false; }

    @ConfigItem(
            keyName = "hideMinimap",
            name = "Hide Minimap",
            description = "Hides the minimap background.",
            position = 9
    )
    default boolean hideMinimap() { return false; }

    @ConfigItem(
            keyName = "hideHudBorder",
            name = "Hide Minimap Border",
            description = "Hides the border ring.",
            position = 10
    )
    default boolean hideHudBorder() { return false; }

    // ---------------- NEW: Offsets (allow negatives) ----------------

    @Range(min = -5000, max = 5000)
    @ConfigItem(
            keyName = "healthOffsetX",
            name = "HP X Offset",
            description = "Horizontal offset for the Hitpoints orb (negative allowed).",
            position = 20
    )
    default int healthOffsetX() { return 0; }

    @Range(min = -5000, max = 5000)
    @ConfigItem(
            keyName = "healthOffsetY",
            name = "HP Y Offset",
            description = "Vertical offset for the Hitpoints orb (negative allowed).",
            position = 21
    )
    default int healthOffsetY() { return 0; }

    @Range(min = -5000, max = 5000)
    @ConfigItem(
            keyName = "prayerOffsetX",
            name = "Prayer X Offset",
            description = "Horizontal offset for the Prayer orb (negative allowed).",
            position = 22
    )
    default int prayerOffsetX() { return 0; }

    @Range(min = -5000, max = 5000)
    @ConfigItem(
            keyName = "prayerOffsetY",
            name = "Prayer Y Offset",
            description = "Vertical offset for the Prayer orb (negative allowed).",
            position = 23
    )
    default int prayerOffsetY() { return 0; }

    @Range(min = -5000, max = 5000)
    @ConfigItem(
            keyName = "runOffsetX",
            name = "Run X Offset",
            description = "Horizontal offset for the Run orb (negative allowed).",
            position = 24
    )
    default int runOffsetX() { return 0; }

    @Range(min = -5000, max = 5000)
    @ConfigItem(
            keyName = "runOffsetY",
            name = "Run Y Offset",
            description = "Vertical offset for the Run orb (negative allowed).",
            position = 25
    )
    default int runOffsetY() { return 0; }

    @Range(min = -5000, max = 5000)
    @ConfigItem(
            keyName = "specOffsetX",
            name = "Spec X Offset",
            description = "Horizontal offset for the Special Attack orb (negative allowed).",
            position = 26
    )
    default int specOffsetX() { return 0; }

    @Range(min = -5000, max = 5000)
    @ConfigItem(
            keyName = "specOffsetY",
            name = "Spec Y Offset",
            description = "Vertical offset for the Special Attack orb (negative allowed).",
            position = 27
    )
    default int specOffsetY() { return 0; }
}