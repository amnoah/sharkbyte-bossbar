package sharkbyte.bossbar.core.modern;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBossBar;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import sharkbyte.bossbar.core.SBBossBar;

import java.util.EnumSet;
import java.util.UUID;

/**
 * This class represents and handles a boss bar for a user in 1.9+.
 * This implementation is meant to be basic, not ideal.
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.1.0
 */
public class ModernBossBar implements SBBossBar {

    private final User user;
    private final UUID barUUID;

    private boolean createFog, darkenSky, playBossMusic;
    private BossBar.Color color;
    private BossBar.Overlay division;
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
    public ModernBossBar(User user, String text, float health, @Nullable BossBar.Color color, @Nullable BossBar.Overlay division) {
        this.barUUID = UUID.randomUUID();
        this.user = user;

        this.text = text;
        this.health = health;
        if (color == null) this.color = BossBar.Color.PINK;
        else this.color = color;
        if (division == null) this.division = BossBar.Overlay.PROGRESS;
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
    public void setColor(BossBar.Color color) {
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
    public void setDivision(BossBar.Overlay division) {
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

        // I don't like the wrapper PacketEvents made. This is the weirdest initialization process yet.
        WrapperPlayServerBossBar wrapper = new WrapperPlayServerBossBar(
                barUUID,
                WrapperPlayServerBossBar.Action.ADD
        );
        wrapper.setTitle(Component.text(text));
        wrapper.setHealth(health);
        wrapper.setColor(color);
        wrapper.setOverlay(division);

        EnumSet<BossBar.Flag> flags = EnumSet.noneOf(BossBar.Flag.class);
        if (darkenSky) flags.add(BossBar.Flag.DARKEN_SCREEN);
        if (playBossMusic) flags.add(BossBar.Flag.PLAY_BOSS_MUSIC);
        if (createFog) flags.add(BossBar.Flag.CREATE_WORLD_FOG);
        wrapper.setFlags(flags);

        user.sendPacket(wrapper);

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
                WrapperPlayServerBossBar.Action.REMOVE
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
        WrapperPlayServerBossBar wrapper;

        if (changedColorOrDivision) {
            wrapper = new WrapperPlayServerBossBar(
                    barUUID,
                    WrapperPlayServerBossBar.Action.UPDATE_STYLE
            );
            wrapper.setColor(color);
            wrapper.setOverlay(division);
            user.writePacket(wrapper);

            changedColorOrDivision = false;
        }

        if (changedFlags) {
            wrapper = new WrapperPlayServerBossBar(
                    barUUID,
                    WrapperPlayServerBossBar.Action.UPDATE_FLAGS
            );
            EnumSet<BossBar.Flag> flags = EnumSet.noneOf(BossBar.Flag.class);
            if (darkenSky) flags.add(BossBar.Flag.DARKEN_SCREEN);
            if (playBossMusic) flags.add(BossBar.Flag.PLAY_BOSS_MUSIC);
            if (createFog) flags.add(BossBar.Flag.CREATE_WORLD_FOG);
            wrapper.setFlags(flags);
            user.writePacket(wrapper);
        }

        if (changedHealth) {
            wrapper = new WrapperPlayServerBossBar(
                    barUUID,
                    WrapperPlayServerBossBar.Action.UPDATE_HEALTH
            );
            wrapper.setHealth(health);
            user.writePacket(wrapper);

            changedHealth = false;
        }

        if (changedText) {
            wrapper = new WrapperPlayServerBossBar(
                    barUUID,
                    WrapperPlayServerBossBar.Action.UPDATE_TITLE
            );
            wrapper.setTitle(Component.text(text));
            user.writePacket(wrapper);

            changedText = false;
        }

        user.flushPackets();
    }
}
