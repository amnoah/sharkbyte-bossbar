package sharkbyte.bossbar.core.impl;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import sharkbyte.bossbar.core.BossBar;
import sharkbyte.bossbar.core.util.LegacyHandler;
import sharkbyte.bossbar.core.util.WrapperPlayServerBossBar;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents and handles a boss bar for a user in 1.8.
 * It is incredibly scuffed currently with major issues surrounding entity culling.
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.0.0
 */
public class LegacyBossBar implements BossBar {

    private final User user;
    private int entityID;

    private float health;
    private String text;

    private boolean created, changedMetadata;

    /**
     * Initialize the LegacyBossBar object.
     */
    public LegacyBossBar(User user) {
        this(user, "");
    }

    /**
     * Initialize the LegacyBossBar object with text.
     */
    public LegacyBossBar(User user, String text) {
        this(user, text, 1);
    }

    /**
     * Initialize the LegacyBossBar object with all settings.
     */
    public LegacyBossBar(User user, String text, float health) {
        this.user = user;
        this.text = text;

        this.health = health;

        created = false;
    }

    /*
     * Setters.
     */

    /**
     * This feature was introduced in 1.9. Will do nothing in 1.8.
     */
    @Override
    public void setColor(WrapperPlayServerBossBar.Color color) {
        // Feature introduced in 1.9.
    }

    /**
     * This feature was introduced in 1.9. Will do nothing in 1.8.
     */
    @Override
    public void setDivision(WrapperPlayServerBossBar.Division division) {
        // Feature introduced in 1.9.
    }

    /**
     * Set the Boss Bar's health.
     * Intended to be (0.0, 1.0], funny stuff happens at 1.43+.
     * You can overflow the bar and make it dead by putting in a large number like 99999999.
     */
    @Override
    public void setHealth(float health) {
        if (this.health == health) return;
        this.health = health;
        changedMetadata = true;
    }

    /**
     * Set the Boss Bar's text.
     */
    @Override
    public void setText(String text) {
        if (this.text.equals(text)) return;
        this.text = text;
        changedMetadata = true;
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

        entityID = LegacyHandler.registerUserToEntity(user, this);

        List<EntityData> metaData = new ArrayList<>();

        // Set the entity as Invisible.
        metaData.add(new EntityData(
                0,
                EntityDataTypes.BYTE,
                (byte) 0x20
        ));
        // Set the entity's Name Tag.
        metaData.add(new EntityData(
                2,
                EntityDataTypes.STRING,
                text
        ));
        // Set Always Show Name Tag to true.
        metaData.add(new EntityData(
                3,
                EntityDataTypes.BYTE,
                (byte) 1
        ));
        // Set Silent to true.
        metaData.add(new EntityData(
                4,
                EntityDataTypes.BYTE,
                (byte) 1
        ));
        // Set the entity's Health.
        metaData.add(new EntityData(
                6,
                EntityDataTypes.FLOAT,
                health * 300
        ));

        user.sendPacket(new WrapperPlayServerSpawnLivingEntity(
                entityID,
                null,
                EntityTypes.WITHER,
                new Location(0, 0, 0, 0, 0),
                0f,
                new Vector3d(0, 0, 0),
                metaData
        ));

        created = true;
        changedMetadata = false;
    }

    /**
     * Calling this method will remove the boss bar from the client.
     */
    @Override
    public void destroy() {
        if (!created) return;

        LegacyHandler.removeUserFromEntity(user, entityID);
        user.sendPacket(new WrapperPlayServerDestroyEntities(
                entityID
        ));

        created = false;
    }

    /**
     * This is intended for use only by the LegacyHandler.
     * Entities are not kept between dimensions, so we must spawn a new copy of the entity when switching dimensions.
     */
    public void handleDimensionSwitch() {
        if (!created) return;

        user.writePacket(new WrapperPlayServerDestroyEntities(
                entityID
        ));

        List<EntityData> metaData = new ArrayList<>();

        // Set the entity as Invisible.
        metaData.add(new EntityData(
                0,
                EntityDataTypes.BYTE,
                (byte) 0x20
        ));
        // Set the entity's Name Tag.
        metaData.add(new EntityData(
                2,
                EntityDataTypes.STRING,
                text
        ));
        // Set Always Show Name Tag to true.
        metaData.add(new EntityData(
                3,
                EntityDataTypes.BYTE,
                (byte) 1
        ));
        // Set Silent to true.
        metaData.add(new EntityData(
                4,
                EntityDataTypes.BYTE,
                (byte) 1
        ));
        // Set the entity's Health.
        metaData.add(new EntityData(
                6,
                EntityDataTypes.FLOAT,
                health * 300
        ));

        user.writePacket(new WrapperPlayServerSpawnLivingEntity(
                entityID,
                null,
                EntityTypes.WITHER,
                new Location(0, 0, 0, 0, 0),
                0f,
                new Vector3d(0, 0, 0),
                metaData
        ));

        user.flushPackets();
        user.sendMessage("Switch Dimensions");
    }

    /**
     * Calling this method will send out all appropriate packets to update the client's boss bar.
     * Don't be afraid to call this often, it will only send packets when required.
     */
    @Override
    public void update() {
        if (!created) return;
        if (!changedMetadata) return;

        List<EntityData> metaData = new ArrayList<>();

        metaData.add(new EntityData(
                2,
                EntityDataTypes.STRING,
                text
        ));
        metaData.add(new EntityData(
                6,
                EntityDataTypes.FLOAT,
                300 * health
        ));

        user.sendPacket(new WrapperPlayServerEntityMetadata(
                entityID,
                metaData
        ));
    }
}
