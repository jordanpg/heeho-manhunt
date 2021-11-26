package dev.jordanpg.heehomanhunt;

import dev.jordanpg.heehomanhunt.data.ManhuntTracker;
import org.bukkit.*;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.*;
import java.util.logging.Logger;

// Manager for manhunt game state
public class ManhuntManager {
    /**
     * Possible manhunt roles
     * Spectators are uninvolved players
     * Runners are speedrunners; the game will end when all speedrunners have died.
     * Hunters seek to kill speedrunners and receive compasses to track runners.
     */
    public enum ManhuntRole {
        Spectator,
        Runner,
        Hunter
    }

    private static final Map<World.Environment, String> environmentDisplayNames = Map.of(
            World.Environment.NETHER, " Nether ",
            World.Environment.THE_END, " End ",
            World.Environment.NORMAL, " Overworld ",
            World.Environment.CUSTOM, " "
    );

    private HashMap<UUID, ManhuntRole> roles;
    private HashMap<UUID, ManhuntTracker> trackers;
    private HashMap<UUID, HashMap<UUID, Location>> portals;
    private ManhuntRole defaultRole = ManhuntRole.Spectator;
    private HeehoManhunt plugin;

    public ManhuntManager(HeehoManhunt plugin)
    {
        this.plugin = plugin;

        roles = new HashMap<>();
        trackers = new HashMap<>();
        portals = new HashMap<>();
    }

    /**
     * Updates a player's manhunt role
     * @param player    Name of player to modify
     * @param role      New role to give to player
     */
    public void setRole(UUID player, ManhuntRole role)
    {
        roles.put(player, role);
    }


    /**
     * @param player    UUID to look up
     * @return          The role of the requested player, or null if the player is unknown
     */
    public ManhuntRole getRole(UUID player) { return roles.getOrDefault(player, null); }
    public ManhuntRole getRole(Player player) { return getRole(player.getUniqueId()); }

    /**
     * Search by role and get a list of online players with that role
     * @param role  Role to search
     * @return      A list containing all online players with the chosen role
     */
    public List<Player> getRolePlayers(ManhuntRole role)
    {
        List<Player> players = new ArrayList<>();

        roles.forEach((id, r) -> {
            Player p = Bukkit.getPlayer(id);
            if(p != null && getRole(p) == role) players.add(p);
        });

        return players;
    }


    /**
     * Check if a player is part of manhunt
     * @param player    Player UUID to check
     * @return          Returns true if this player is a hunter or runner.
     */
    public boolean isManhuntPlayer(UUID player) {
        ManhuntRole r = getRole(player);
        return r == ManhuntRole.Hunter || r == ManhuntRole.Runner;
    }

    /**
     * Search by role and get a list of all players (online or offline) with that role
     * @param role  Role to search
     * @return      A list containing all players with the chosen role
     */
    public List<OfflinePlayer> getRoleOnlinePlayers(ManhuntRole role)
    {
        List<OfflinePlayer> players = new ArrayList<>();

        roles.forEach((id, r) -> {
            OfflinePlayer p = Bukkit.getOfflinePlayer(id);
            if(p != null) players.add(p);
        });

        return players;
    }

    public Logger getLogger()
    {
        return plugin.getLogger();
    }

    public boolean playerHasTrackableTarget(Player player)
    {
        ManhuntRole role = getRole(player);

        if(role == ManhuntRole.Hunter &&
                getRolePlayers(ManhuntRole.Runner).size() > 0)
            return true;

        if(role == ManhuntRole.Runner &&
                (boolean)plugin.getConfig("runners.locateFortress") &&
                player.getWorld().getEnvironment() == World.Environment.NETHER)
            return true;

        return false;
    }

    /**
     * Fetches or creates a ManhuntTracker for this player
     * @param playerId Player UUID to look up
     * @return A ManhuntTracker instance associated with this player
     */
    public ManhuntTracker getTracker(UUID playerId)
    {
        if(!trackers.containsKey(playerId)) {
            ManhuntTracker t = new ManhuntTracker();
            trackers.put(playerId, t);
            return t;
        }
        return trackers.get(playerId);
    }

    /**
     * Update tracking information for this player
     * @param player Player to update
     */
    public void updateTracker(Player player)
    {
        UUID playerId = player.getUniqueId();
        ManhuntTracker tracker = getTracker(playerId);

        if(!playerHasTrackableTarget(player)) {
            tracker.tracking = null;
            tracker.lastLocation = null;
            return;
        }

        // Handle tracker for hunters
        if(getRole(playerId) == ManhuntRole.Hunter)
        {
            Player target = null;

            // Make sure tracking target is still a runner
            if(tracker.tracking != null)
            {
                target = Bukkit.getPlayer(tracker.tracking);
                if(getRole(target) != ManhuntRole.Runner) tracker.tracking = null;
            }

            // Set default tracking target if necessary
            if(tracker.tracking == null)
            {
                target = getRolePlayers(ManhuntRole.Runner).get(0);
                tracker.tracking = target.getName();
            }

            // Check if target and player are in the same world
            World world = player.getWorld();
            if(target.getWorld() != world) {
                HashMap<UUID, Location> portalMap = getPortalMap(target.getUniqueId());

                // Check if the target has entered a portal in this world
                if(!portalMap.containsKey(world.getUID())) {
                    tracker.lastLocation = null;
                    return;
                }

                tracker.displayName = tracker.tracking + "'s" + environmentDisplayNames.get(target.getWorld().getEnvironment()) + "portal";
                tracker.lastLocation = portalMap.get(world.getUID());
            }
            else
            {
                tracker.displayName = tracker.tracking;
                tracker.lastLocation = target.getLocation();
            }
        }
        // Handle tracker for runners
        else if(getRole(playerId) == ManhuntRole.Runner)
        {
            // Runners are only eligible for tracking if:
            // a. They are in the nether
            // b. The configuration to locate nether fortresses is enabled

            tracker.tracking = "fortress";
            tracker.displayName = "Nether Fortress";
            tracker.lastLocation = player.getWorld().locateNearestStructure(
                    player.getLocation(),
                    StructureType.NETHER_FORTRESS,
                    100,
                    false);

            // locateNearestStructure only gives XZ coordinates;
            // set Y to player's current Y to avoid sending them to the bottom of the world
            tracker.lastLocation.setY(player.getLocation().getY());
        }
    }

    /**
     * Save a location as a player's most recently entered portal in that world
     * @param player Player to assign this portal location to
     * @param portalLocation Location of the portal
     */
    public void setPortal(Player player, Location portalLocation)
    {
        getPortalMap(player.getUniqueId()).put(portalLocation.getWorld().getUID(), portalLocation);
    }

    /**
     * Fetches or creates this player's map of last-entered portals
     * @param pid Player ID to look up
     * @return A HashMap mapping a world UUID to a Location object of a portal
     */
    private HashMap<UUID, Location> getPortalMap(UUID pid)
    {
        if(!portals.containsKey(pid))
        {
            HashMap<UUID, Location> newMap = portals.put(pid, new HashMap<>());
            portals.put(pid, newMap);
            return newMap;
        }
        else
            return portals.get(pid);
    }
}
