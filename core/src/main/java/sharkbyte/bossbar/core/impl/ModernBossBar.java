package sharkbyte.bossbar.core.impl;

import com.github.retrooper.packetevents.protocol.player.User;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import sharkbyte.bossbar.core.BossBar;
import sharkbyte.bossbar.core.util.WrapperPlayServerBossBar;

import java.util.UUID;

/**
 * This class represents and handles a boss bar for a user in 1.9+.
 * This implementation is meant to be basic, not ideal.
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.0.0
 */
public class ModernBossBar implements BossBar {

    private final User user;
    private final UUID barUUID;

    private boolean createFog, darkenSky, playBossMusic;
    private WrapperPlayServerBossBar.Color color;
    private WrapperPlayServerBossBar.Division division;
    private float health;
    private String text;

    private boolean created, changedColorOrDivision, changedFlags, changedHealth, changedText;

    /**
     * Initialize the ModernBossBar object.
     */
    public ModernBossBar(User user) {
        this(user, "");
    }

    /**
     * Initialize the ModernBossBar object with text.
     */
    public ModernBossBar(User user, String text) {
        this(user, text, 1, null, null);
    }

    /**
     * Initialize the ModernBossBar object with all settings.
     */
    public ModernBossBar(User user, String text, float health, @Nullable WrapperPlayServerBossBar.Color color, @Nullable WrapperPlayServerBossBar.Division division) {
        this.barUUID = UUID.randomUUID();
        this.user = user;

        this.text = text;
        this.health = health;
        if (color == null) this.color = WrapperPlayServerBossBar.Color.PINK;
        else this.color = color;
        if (division == null) this.division = WrapperPlayServerBossBar.Division.NONE;
        else this.division = division;
        createFog = darkenSky = playBossMusic = false;

        created = false;
    }

    /*
     * Setters.
     */

    /**
     * Set the Boss Bar's color.
     */
    @Override
    public void setColor(WrapperPlayServerBossBar.Color color) {
        if (this.color.equals(color)) return;
        this.color = color;
        changedColorOrDivision = true;
    }

    /**
     * Set whether the boss bar should create fog.
     */
    @Override
    public void setCreateFog(boolean createFog) {
        if (this.createFog == createFog) return;
        this.createFog = createFog;
        changedFlags = true;
    }

    /**
     * Set whether the boss bar should darken the sky.
     */
    @Override
    public void setDarkenSky(boolean darkenSky) {
        if (this.darkenSky == darkenSky) return;
        this.darkenSky = darkenSky;
        changedFlags = true;
    }

    /**
     * Set the Boss Bar's division mode.
     */
    @Override
    public void setDivision(WrapperPlayServerBossBar.Division division) {
        if (this.division.equals(division)) return;
        this.division = division;
        changedColorOrDivision = true;
    }

    /**
     * Set the Boss Bar's health.
     * Intended to be 0.0 - 1.0 , funny stuff happens at 2.13+.
     */
    @Override
    public void setHealth(float health) {
        if (this.health == health) return;
        this.health = health;
        changedHealth = true;
    }

    /**
     * Set whether the boss bar should play music.
     */
    @Override
    public void setPlayBossMusic(boolean playBossMusic) {
        if (this.playBossMusic == playBossMusic) return;
        this.playBossMusic = playBossMusic;
        changedFlags = true;
    }

    /**
     * Set the Boss Bar's text.
     */
    @Override
    public void setText(String text) {
        if (this.text.equals(text)) return;
        this.text = text;
        changedText = true;
    }

    /*
     * Boss Bar Handlers.
     */

    /**
     * Calling this method will create the boss bar inside the client.
     */
    @Override
    public void create() {
        if (created) return;

        user.sendPacket(new WrapperPlayServerBossBar(
                barUUID,
                WrapperPlayServerBossBar.Action.add(
                        Component.text(text),
                        health,
                        color,
                        division,
                        WrapperPlayServerBossBar.createFlags(
                                darkenSky,
                                playBossMusic,
                                createFog
                        )
                )
        ));

        changedColorOrDivision = changedFlags = changedHealth = changedText = false;
        created = true;
    }

    /**
     * Calling this method will remove the boss bar from the client.
     */
    @Override
    public void destroy() {
        if (!created) return;

        user.sendPacket(new WrapperPlayServerBossBar(
                barUUID,
                WrapperPlayServerBossBar.Action.remove()
        ));

        created = false;
    }

    /**
     * Calling this method will send out all appropriate packets to update the client's boss bar.
     * Don't be afraid to call this often, it will only send packets when required.
     */
    @Override
    public void update() {
        if (!created) return;
        if (!changedColorOrDivision && !changedHealth && !changedText) return;

        if (changedColorOrDivision) {
            user.writePacket(new WrapperPlayServerBossBar(
                    barUUID,
                    WrapperPlayServerBossBar.Action.updateStyle(
                            color,
                            division
                    )
            ));

            changedColorOrDivision = false;
        }

        if (changedFlags) {
            user.writePacket(new WrapperPlayServerBossBar(
                    barUUID,
                    WrapperPlayServerBossBar.Action.updateFlags(
                            WrapperPlayServerBossBar.createFlags(
                                    darkenSky,
                                    playBossMusic,
                                    createFog
                            )
                    )
            ));
        }

        if (changedHealth) {
            user.writePacket(new WrapperPlayServerBossBar(
                    barUUID,
                    WrapperPlayServerBossBar.Action.updateHealth(health)
            ));

            changedHealth = false;
        }

        if (changedText) {
            user.writePacket(new WrapperPlayServerBossBar(
                    barUUID,
                    WrapperPlayServerBossBar.Action.updateTitle(Component.text(text))
            ));

            changedText = false;
        }

        user.flushPackets();
    }
}
