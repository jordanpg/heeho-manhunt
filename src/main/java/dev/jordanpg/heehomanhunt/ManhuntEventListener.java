package dev.jordanpg.heehomanhunt;

import dev.jordanpg.heehomanhunt.data.ManhuntTracker;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.awt.print.Paper;
import java.util.UUID;

public class ManhuntEventListener implements Listener {
    ManhuntManager manager;

    public ManhuntEventListener(ManhuntManager manager)
    {
        this.manager = manager;
    }

    /**
     * Handles interacting with a compass to update tracking
     * @param event PlayerInteractEvent
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        UUID pid = p.getUniqueId();

        // Don't do anything for players not playing manhunt
        if(!manager.isManhuntPlayer(pid)) return;

        if(event.getMaterial() == Material.COMPASS)
        {
            // Update tracking info for this player
            manager.updateTracker(p);
            ManhuntTracker tracker = manager.getTracker(pid);

            // Handle case where no target is found for this player
            if(tracker.lastLocation == null){
                TextComponent.Builder builder = Component.text()
                        .color(TextColor.color(0xff5555));

                if(tracker.tracking != null)
                    builder.content("Could not track ")
                        .append(Component.text()
                        .color(TextColor.color(0xff5555))
                        .decorate(TextDecoration.BOLD)
                        .content(tracker.tracking)
                    );
                else
                    builder.content("No trackable targets found!");

                p.sendActionBar(builder.build());

                return;
            }

            // Update held compass target
            ItemStack item = event.getItem();
            CompassMeta meta = (CompassMeta)item.getItemMeta();
            meta.setLodestone(tracker.lastLocation);
            meta.setLodestoneTracked(false);
            item.setItemMeta(meta);

            p.setCompassTarget(tracker.lastLocation);

            // Calculate distance to target
            double dist = tracker.lastLocation.distance(p.getLocation());

            // Create action bar message
            final TextComponent text = Component.text()
                    .color(TextColor.color(0x55ff55))
                    .content("Tracking: ")
                    .append(Component.text()
                        .color(TextColor.color(0xffffff))
                        .content(tracker.displayName)
                    )
                    .append(Component.text()
                        .color(TextColor.color(0x55ff55))
                        .content(" | Distance: ")
                    )
                    .append(Component.text()
                        .color(TextColor.color(0xffffff))
                        .content(String.format("%,.1f", dist))
                    )
                    .build();
            p.sendActionBar(text);

            // manager.getLogger().info(PlainTextComponentSerializer.plainText().serialize(text));
        }
    }

    /**
     * Handles updating last portal locations for players
     * @param event PlayerTeleportEvent
     */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player p = event.getPlayer();
        UUID pid = p.getUniqueId();

        // Don't do anything for players not in manhunt
        if(!manager.isManhuntPlayer(pid)) return;

        // If the player entered a portal, then let the manager know the location of the portal
        if(event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL || event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            manager.setPortal(p, event.getFrom());
        }
    }
}
