package sharkbyte.bossbar.core.legacy;

/**
 * This isn't really safe... so maybe you should set an entity ID provider.
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.0.0
 */
public class DefaultEntityIDProvider implements EntityIDProvider{

    private int entityID = 0;

    @Override
    public int getNextEntityID() {
        return entityID--;
    }
}
