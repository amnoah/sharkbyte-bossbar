package sharkbyte.bossbar.core.util;

/**
 * This is a simple interface meant to generate viable entityIDs for the LegacyHandler. It is ONLY required if you're
 * supporting 1.8.8.
 * If you're on Spigot, I'd recommend to use PacketEvents' SpigotReflectionUtil in a lambda statement like:
 * LegacyHandler.setEntityIDProvider(SpigotReflectionUtil::generateEntityId);
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.0.0
 */
public interface EntityIDProvider {

    /**
     * Generate the next available entityID.
     */
    int getNextEntityID();
}
