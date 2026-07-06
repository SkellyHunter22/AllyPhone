package com.allyphone.map;

import com.allyphone.AllyPhonePlugin;
import com.allyphone.service.CellTowerStore;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Shows registered cell towers as POI markers on BlueMap, if it's installed. */
public class TowerMapIntegration {

    private static final String MARKER_SET_ID = "allyphone-cell-towers";

    private final AllyPhonePlugin plugin;

    public TowerMapIntegration(AllyPhonePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isMapAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("BlueMap");
    }

    /** Registers a BlueMap-enable listener so markers populate whenever BlueMap (re)loads its maps. */
    public void register() {
        if (!isMapAvailable()) return;
        BlueMapAPI.onEnable(api -> refresh());
    }

    /** Re-syncs every registered cell tower onto BlueMap's marker layer. Safe to call often. */
    public void refresh() {
        if (!isMapAvailable()) return;
        Optional<BlueMapAPI> apiOpt = BlueMapAPI.getInstance();
        if (apiOpt.isEmpty()) return;
        BlueMapAPI api = apiOpt.get();

        List<CellTowerStore.CellTower> towers;
        try {
            towers = plugin.getCellTowerStore().getAll();
        } catch (SQLException e) {
            plugin.getLogger().warning("TowerMapIntegration: failed to load towers: " + e.getMessage());
            return;
        }

        Map<String, List<CellTowerStore.CellTower>> byWorld = new HashMap<>();
        for (CellTowerStore.CellTower tower : towers) {
            byWorld.computeIfAbsent(tower.world(), w -> new ArrayList<>()).add(tower);
        }

        for (World world : Bukkit.getWorlds()) {
            Optional<BlueMapWorld> bmWorld = api.getWorld(world);
            if (bmWorld.isEmpty()) continue;

            List<CellTowerStore.CellTower> worldTowers = byWorld.getOrDefault(world.getName(), List.of());
            for (BlueMapMap map : bmWorld.get().getMaps()) {
                MarkerSet set = MarkerSet.builder().label("Cell Towers").build();
                for (CellTowerStore.CellTower tower : worldTowers) {
                    POIMarker marker = POIMarker.builder()
                            .label(tower.name())
                            .position(tower.x() + 0.5, tower.y() + 0.5, tower.z() + 0.5)
                            .detail("Cell Tower: " + tower.name() + "<br>Radius: " + tower.radius())
                            .defaultIcon()
                            .build();
                    set.getMarkers().put("tower-" + tower.id(), marker);
                }
                map.getMarkerSets().put(MARKER_SET_ID, set);
            }
        }
    }
}
