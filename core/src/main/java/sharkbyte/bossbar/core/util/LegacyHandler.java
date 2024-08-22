package sharkbyte.bossbar.core.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import sharkbyte.bossbar.core.impl.LegacyBossBar;

import java.util.*;

/**
 * This class represents and handles a boss bar for a user in 1.8.
 * It is incredibly scuffed currently with major issues surrounding entity culling.
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.0.0
 */
public class LegacyHandler extends SimplePacketListenerAbstract {

    public static double ENTITY_DISTANCE = 15;

    private static boolean registeredListener = false;
    private static EntityIDProvider entityIDProvider;
    private static final Set<FakeEntity> fakeEntities = new HashSet<>();

    private static final Map<User, Location> userLocations = new HashMap<>();

    static {
        entityIDProvider = new DefaultEntityIDProvider();
    }

    /*
     * Setters.
     */

    /**
     * Set how entity IDs should be resolved.
     */
    public static void setEntityIDProvider(EntityIDProvider entityIDProvider) {
        LegacyHandler.entityIDProvider = entityIDProvider;
    }

    /*
     * Entity Manager.
     */

    /**
     * Register the given user and their boss bar to a fake entity, returning the fake entity's entityid.
     */
    public static int registerUserToEntity(User user, LegacyBossBar bossBar) {
        // Lazy load the listener. No use in loading it if the boss bar isn't being used.
        if (!registeredListener) {
            PacketEvents.getAPI().getEventManager().registerListener(new LegacyHandler());
            registeredListener = true;
        }

        // Find if there's any current entities that the player can join.
        for (FakeEntity entity : fakeEntities) {
            if (entity.hasUser(user)) continue;
            entity.addUser(user, bossBar);
            return entity.getId();
        }


        int nextID = entityIDProvider.getNextEntityID();
        FakeEntity newEntity = new FakeEntity(nextID);
        newEntity.addUser(user, bossBar);
        fakeEntities.add(newEntity);
        return nextID;
    }

    /**
     * Remove the given user from the fake entity with the given id.
     */
    public static void removeUserFromEntity(User user, int id) {
        for (FakeEntity entity : fakeEntities) {
            if (entity.getId() != id) continue;
            entity.removeUser(user);
            return;
        }
    }

    /**
     * I'm going to be honest, I'm not even sure if you can have multiple boss bars in 1.8.x, but if you can then this
     * allows you to.
     * This is a fake entity that we can spawn for the player.
     */
    private static class FakeEntity {

        private final int id;
        private final List<User> users;
        private final List<LegacyBossBar> bossBars;

        /**
         * Initialize a FakeEntity with the given id.
         */
        public FakeEntity(int id) {
            this.id = id;
            this.users = new ArrayList<>();
            this.bossBars = new ArrayList<>();
        }

        /*
         * Getters.
         */

        /**
         * Return the fake entity's entity id.
         */
        public int getId() {
            return id;
        }

        /**
         * Return the boss bar associated with the given user.
         */
        public LegacyBossBar getBossBarByUser(User user) {
            int index = users.indexOf(user);
            if (index == -1) return null;
            return bossBars.get(index);
        }

        /*
         * User List Methods.
         */

        /**
         * Add the given user and their boss bar to the entity.
         */
        public void addUser(User user, LegacyBossBar bossBar) {
            users.add(user);
            bossBars.add(bossBar);
        }

        /**
         * Return whether the given user is attached to this entity.
         */
        public boolean hasUser(User user) {
            return users.contains(user);
        }

        /**
         * Remove the given user from this entity.
         */
        public void removeUser(User user) {
            int index = users.indexOf(user);
            if (index == -1) return;
            users.remove(index);
            bossBars.remove(index);
        }
    }

    /*
     * Packet Functions.
     */

    /**
     * Track user location & update wither location when the player updates their position.
     */
    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) return;

        WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
        if (!wrapper.hasPositionChanged() && !wrapper.hasRotationChanged()) return;

        Location userLocation = userLocations.get(event.getUser());
        if (userLocation == null) userLocation = new Location(0, 0, 0, 0, 0);

        if (wrapper.hasPositionChanged()) {
            userLocation.setPosition(new Vector3d(
                    wrapper.getLocation().getX(),
                    wrapper.getLocation().getY(),
                    wrapper.getLocation().getZ()
            ));
        }

        if (wrapper.hasRotationChanged()) {
            userLocation.setPitch(wrapper.getLocation().getPitch());
            userLocation.setYaw(wrapper.getLocation().getYaw());
        }

        userLocations.put(event.getUser(), userLocation);

        double x = 0, y = 0, z = 0;

        boolean wroteTeleport = false, wroteCoords = false;

        for (FakeEntity entity : fakeEntities) {
            if (!entity.hasUser(event.getUser())) continue;
            if (!wroteCoords) {
                // WHY AM I DOING TRIGONOMETRY IN A BOSS BAR PLUGIN. I HEAVILY DISLIKE LEGACY SUPPORT.
                double pitchRadians = Math.toRadians(userLocation.getPitch());
                double yawRadians = Math.toRadians(userLocation.getYaw());

                double ratioY = -Math.sin(pitchRadians);
                double leftover = Math.pow(1 - Math.pow(ratioY, 2), 0.5);
                double ratioX = -Math.sin(yawRadians) * leftover;
                double ratioZ = Math.cos(yawRadians) * leftover;

                x = userLocation.getX() + (ENTITY_DISTANCE * ratioX);
                y = userLocation.getY() + (ENTITY_DISTANCE * ratioY);
                z = userLocation.getZ() + (ENTITY_DISTANCE * ratioZ);
            }


            event.getUser().writePacket(new WrapperPlayServerEntityTeleport(
                    entity.getId(),
                    new Location(
                            x,
                            y,
                            z,
                            userLocation.getYaw(),
                            userLocation.getPitch()
                    ),
                    false
            ));

            wroteTeleport = true;
        }

        if (wroteTeleport) event.getUser().flushPackets();
    }

    /**
     * Listen for outgoing teleports.
     * We don't know whether it's a regular teleport or world change, so we assume it's a world change.
     * If you complain, update to 1.9+.
     */
    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        // Dimension switch.
        if (event.getPacketType() != PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) return;

        for (FakeEntity fakeEntity : fakeEntities) {
            LegacyBossBar bossBar = fakeEntity.getBossBarByUser(event.getUser());
            if (bossBar == null) continue;
            bossBar.handleDimensionSwitch();
        }
    }

    /**
     * Prevent data leak.
     */
    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        userLocations.remove(event.getUser());
        for (FakeEntity fakeEntity : fakeEntities) fakeEntity.removeUser(event.getUser());
    }
}
