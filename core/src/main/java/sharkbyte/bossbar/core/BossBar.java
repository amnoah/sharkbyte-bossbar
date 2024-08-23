package sharkbyte.bossbar.core;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import org.jetbrains.annotations.Nullable;
import sharkbyte.bossbar.core.impl.LegacyBossBar;
import sharkbyte.bossbar.core.impl.ModernBossBar;
import sharkbyte.bossbar.core.util.WrapperPlayServerBossBar;

/**
 * This interface provides a version-independent way of managing Boss Bars despite being inherently version dependent.
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.0.0
 */
public interface BossBar {

    /*
     * Version-Independent Constructors.
     */

    /**
     * Initialize a boss bar with the given parameters.
     */
    static BossBar createBossBar(User user) {
        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) return new ModernBossBar(user);
        else return new LegacyBossBar(user);
    }

    /**
     * Initialize a boss bar with the given parameters.
     */
    static BossBar createBossBar(User user, String text) {
      if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) return new ModernBossBar(user, text);
      else return new LegacyBossBar(user, text);
    }

    /**
     * Initialize a boss bar with the given parameters.
     */
    static BossBar createBossBar(User user, String text, float health, @Nullable WrapperPlayServerBossBar.Color color, @Nullable WrapperPlayServerBossBar.Division division) {
      if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) return new ModernBossBar(user, text, health, color, division);
      else return new LegacyBossBar(user, text, health);
    }

    /*
     * Setters.
     */

    /**
     * Set the boss bar's color.
     * Only works in 1.9+.
     */
    void setColor(WrapperPlayServerBossBar.Color color);

    /**
     * Set whether the boss bar should create fog on the user's screen.
     * Only works in 1.9+.
     */
    void setCreateFog(boolean createFog);

    /**
     * Set whether the boss bar should darken the sky on the user's screen.
     * Only works in 1.9+.
     */
    void setDarkenSky(boolean darkenSky);

    /**
     * Set the boss bar's division.
     * Only works in 1.9.
     */
    void setDivision(WrapperPlayServerBossBar.Division division);

    /**
     * Set the boss bar's health.
     * Values range from 0.0 to 1.0 normally. Larger values can have funny results. Values of 0 or below on 1.8 will
     * break the boss bar and have no effect on 1.9+.
     */
    void setHealth(float health);

    /**
     * Set whether the boss bar should play boss music for the user.
     * Only works in 1.9+.
     */
    void setPlayBossMusic(boolean playBossMusic);

    /**
     * Set the boss bar's text.
     */
    void setText(String text);

    /*
     * Boss Bar Handlers.
     */

    /**
     * Send packets to create the Boss Bar on the client.
     */
    void create();

    /**
     * Send packets to destroy the Boss Bar on the client.
     */
    void destroy();

    /**
     * Send all packets to update Boss Bar settings.
     */
    void update();
}
