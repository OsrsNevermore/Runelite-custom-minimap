package com.orbhidermini;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@PluginDescriptor(
        name = "Custom Minimap",
        description = "Enables additional customization features of minimap and orbs, both minimized and full sized.",
        tags = {"orbs", "orb", "minimap", "spec", "hide", "hider", "logout", "compass"}
)
public class OrbHiderminiPlugin extends Plugin
{
    @Inject private Client client;
    @Inject private ClientThread clientThread;          // <-- ensure all UI writes are on client thread
    @Inject private OrbHiderminiConfig config;

    // --- Nomap (alternate minimap) constants ---
    private static final int NOMAP_GROUP_ID = 895;
    private static final int NOMAP_XP_DROPS       = 4;
    private static final int NOMAP_ORB_HEALTH     = 5;
    private static final int NOMAP_ORB_PRAYER     = 16;
    private static final int NOMAP_ORB_RUNENERGY  = 24;
    private static final int NOMAP_ORB_SPECENERGY = 32;
    private static final int NOMAP_ORB_WORLDMAP   = 51;

    // --- Normal minimap group ---
    // This matches InterfaceID.MINIMAP
    private static final int MINIMAP_GROUP_ID   = 164;

    // Logout (X) button pieces
    private static final int MINIMAP_X_STONE10  = 34;
    private static final int MINIMAP_X_ICON10   = 35;

    // Make minimap/compass area click-through
    private static final int[] MINIMAP_HUD_CLICK = {23, 24, 25, 26, 27, 28, 31};

    // Individual UI bits you asked to control:
    private static final int MINIMAP_COMPASS_ID    = 29; // Compass graphic
    private static final int MINIMAP_MINIMAP_ID    = 30; // Minimap disc/background
    private static final int MINIMAP_HUD_BORDER_ID = 32; // HUD border ring

    // ---------------- Baseline storage for "0 = original" (minimal feature addition) ----------------
    // Key format "groupId:childId" -> original (x,y)
    private final Map<String, int[]> baselineXY = new HashMap<>();
    private static String k(int groupId, int childId) { return groupId + ":" + childId; }

    private void captureBaselineIfMissing(int groupId, int childId, Widget w)
    {
        String key = k(groupId, childId);
        if (!baselineXY.containsKey(key))
        {
            baselineXY.put(key, new int[]{ w.getOriginalX(), w.getOriginalY() });
        }
    }

    private void setFromBaseline(int groupId, int childId, Widget w, int dx, int dy)
    {
        String key = k(groupId, childId);
        int[] base = baselineXY.get(key);
        if (base == null)
        {
            base = new int[]{ w.getOriginalX(), w.getOriginalY() };
            baselineXY.put(key, base);
        }
        w.setOriginalX(base[0] + dx);
        w.setOriginalY(base[1] + dy);
        w.revalidate();
    }

    // Apply offsets to both the normal and the nomap version of a given orb
    private void applyOffsets(int normalComponentId, int nomapChildId, int dx, int dy)
    {
        Widget normal = client.getWidget(normalComponentId);
        if (normal != null)
        {
            int normalChildId = normal.getId() & 0xFFFF;
            captureBaselineIfMissing(MINIMAP_GROUP_ID, normalChildId, normal);
            setFromBaseline(MINIMAP_GROUP_ID, normalChildId, normal, dx, dy);
        }

        Widget nomap = client.getWidget(NOMAP_GROUP_ID, nomapChildId);
        if (nomap != null)
        {
            captureBaselineIfMissing(NOMAP_GROUP_ID, nomapChildId, nomap);
            setFromBaseline(NOMAP_GROUP_ID, nomapChildId, nomap, dx, dy);
        }
    }
    // ------------------------------------------------------------------------------------------------

    @Provides
    OrbHiderminiConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(OrbHiderminiConfig.class);
    }

    @Override
    protected void startUp()
    {
        clientThread.invoke(this::updateAllOrbs);        // run on client thread
    }

    @Override
    protected void shutDown()
    {
        clientThread.invoke(this::showAllOrbs);          // run on client thread
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        int groupId = event.getGroupId();
        if (groupId == InterfaceID.MINIMAP || groupId == NOMAP_GROUP_ID)
        {
            clientThread.invoke(this::updateAllOrbs);    // run on client thread
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        clientThread.invoke(this::updateAllOrbs);        // run on client thread
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired e)
    {
        if (!config.hideLogoutButton()) return;

        // If any script just changed UI, ensure the X stays hidden.
        Widget stone = client.getWidget(MINIMAP_GROUP_ID, MINIMAP_X_STONE10);
        Widget icon  = client.getWidget(MINIMAP_GROUP_ID, MINIMAP_X_ICON10);

        if (stone != null && !stone.isHidden()) stone.setHidden(true);
        if (icon  != null && !icon.isHidden())  icon.setHidden(true);
    }

    /* ---------------- Helpers ---------------- */

    private void setHidden(Widget w, boolean hidden)
    {
        if (w != null) w.setHidden(hidden);
    }

    private void setOrbHidden(int normalComponentId, int nomapChildId, boolean hidden)
    {
        setHidden(client.getWidget(normalComponentId), hidden);            // Normal minimap
        setHidden(client.getWidget(NOMAP_GROUP_ID, nomapChildId), hidden); // Nomap minimap
    }

    private void hideChildren(int groupId, int[] childIds, boolean hidden)
    {
        for (int childId : childIds)
        {
            Widget w = client.getWidget(groupId, childId);
            if (w != null) w.setHidden(hidden);
        }
    }

    private void hideLogoutX(boolean hidden)
    {
        setHidden(client.getWidget(MINIMAP_GROUP_ID, MINIMAP_X_STONE10), hidden);
        setHidden(client.getWidget(MINIMAP_GROUP_ID, MINIMAP_X_ICON10),  hidden);
    }

    private void setMinimapClickThrough(boolean enabled)
    {
        // enabled == true => hide the click-blocking layers; area becomes click-through
        hideChildren(MINIMAP_GROUP_ID, MINIMAP_HUD_CLICK, enabled);
    }

    private void hideCompassUI(boolean hidden)
    {
        setHidden(client.getWidget(MINIMAP_GROUP_ID, MINIMAP_COMPASS_ID), hidden);
    }

    private void hideMinimapUI(boolean hidden)
    {
        setHidden(client.getWidget(MINIMAP_GROUP_ID, MINIMAP_MINIMAP_ID), hidden);
    }

    private void hideHudBorderUI(boolean hidden)
    {
        setHidden(client.getWidget(MINIMAP_GROUP_ID, MINIMAP_HUD_BORDER_ID), hidden);
    }

    /* ---------------- Apply config ---------------- */

    public void updateAllOrbs()
    {
        setOrbHidden(ComponentID.MINIMAP_XP_ORB,        NOMAP_XP_DROPS,       config.hideXpOrb());
        setOrbHidden(ComponentID.MINIMAP_HEALTH_ORB,    NOMAP_ORB_HEALTH,     config.hideHealthOrb());
        setOrbHidden(ComponentID.MINIMAP_PRAYER_ORB,    NOMAP_ORB_PRAYER,     config.hidePrayerOrb());
        setOrbHidden(ComponentID.MINIMAP_RUN_ORB,       NOMAP_ORB_RUNENERGY,  config.hideRunOrb());
        setOrbHidden(ComponentID.MINIMAP_SPEC_ORB,      NOMAP_ORB_SPECENERGY, config.hideSpecOrb());
        setOrbHidden(ComponentID.MINIMAP_WORLDMAP_ORB,  NOMAP_ORB_WORLDMAP,   config.hideWorldMapOrb());

        // Independent toggles
        hideLogoutX(config.hideLogoutButton());
        setMinimapClickThrough(config.disableHUDClicking());

        // Independent controls for the three pieces
        hideCompassUI(config.hideCompass());     // child 29
        hideMinimapUI(config.hideMinimap());     // child 30
        hideHudBorderUI(config.hideHudBorder()); // child 32

        // --- Minimal feature addition: apply X/Y offsets for HP/Prayer/Run/Spec (0 = original) ---
        applyOffsets(ComponentID.MINIMAP_HEALTH_ORB,    NOMAP_ORB_HEALTH,     config.healthOffsetX(),   config.healthOffsetY());
        applyOffsets(ComponentID.MINIMAP_PRAYER_ORB,    NOMAP_ORB_PRAYER,     config.prayerOffsetX(),   config.prayerOffsetY());
        applyOffsets(ComponentID.MINIMAP_RUN_ORB,       NOMAP_ORB_RUNENERGY,  config.runOffsetX(),      config.runOffsetY());
        applyOffsets(ComponentID.MINIMAP_SPEC_ORB,      NOMAP_ORB_SPECENERGY, config.specOffsetX(),     config.specOffsetY());
        // ------------------------------------------------------------------------------------------
    }

    public void showAllOrbs()
    {
        setOrbHidden(ComponentID.MINIMAP_XP_ORB,        NOMAP_XP_DROPS,       false);
        setOrbHidden(ComponentID.MINIMAP_HEALTH_ORB,    NOMAP_ORB_HEALTH,     false);
        setOrbHidden(ComponentID.MINIMAP_PRAYER_ORB,    NOMAP_ORB_PRAYER,     false);
        setOrbHidden(ComponentID.MINIMAP_RUN_ORB,       NOMAP_ORB_RUNENERGY,  false);
        setOrbHidden(ComponentID.MINIMAP_SPEC_ORB,      NOMAP_ORB_SPECENERGY, false);
        setOrbHidden(ComponentID.MINIMAP_WORLDMAP_ORB,  NOMAP_ORB_WORLDMAP,   false);

        hideLogoutX(false);
        setMinimapClickThrough(false);
        hideCompassUI(false);
        hideMinimapUI(false);
        hideHudBorderUI(false);
    }
}
