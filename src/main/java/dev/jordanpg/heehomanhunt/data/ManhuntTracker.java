package dev.jordanpg.heehomanhunt.data;

import org.bukkit.Location;

public class ManhuntTracker {
    public String tracking = null;
    public Location lastLocation = null;
    public String displayName = null;

    public ManhuntTracker(String tracking, Location lastLocation)
    {
        this.tracking = tracking;
        this.displayName = tracking;
        this.lastLocation = lastLocation;
    }

    public ManhuntTracker(String tracking, Location lastLocation, String displayName)
    {
        this.tracking = tracking;
        this.displayName = displayName;
        this.lastLocation = lastLocation;
    }

    public ManhuntTracker() {}
}
