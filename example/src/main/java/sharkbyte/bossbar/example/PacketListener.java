package sharkbyte.bossbar.example;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import net.kyori.adventure.bossbar.BossBar;
import sharkbyte.bossbar.core.SBBossBar;
import sharkbyte.bossbar.core.legacy.LegacyHandler;

/**
 * This class shows basic usage of the BossBar.
 *
 * @Author: am noah
 * @Since: 1.0.0
 * @Updated: 1.0.0
 */
public class PacketListener extends SimplePacketListenerAbstract {

    static {
        LegacyHandler.setEntityIDProvider(SpigotReflectionUtil::generateEntityId);
    }

    private SBBossBar bossBar;

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (!event.getPacketType().equals(PacketType.Play.Client.CHAT_MESSAGE)) return;

        if (bossBar == null) {
            bossBar = SBBossBar.createBossBar(event.getUser(), "This is a boss bar!", 1, null, null);
        }

        WrapperPlayClientChatMessage message = new WrapperPlayClientChatMessage(event);

        String[] elements = message.getMessage().split(" ", 2);

        switch (elements[0]) {
            case "create":
                bossBar.create();
                break;
            case "text":
                bossBar.setText(elements[1]);
                break;
            case "destroy":
                bossBar.destroy();
                break;
            case "health":
                float health;

                try {
                    health = Float.parseFloat(elements[1]);
                } catch (Exception e) {
                    health = 1;
                }

                bossBar.setHealth(health);
                break;
            case "color":
                switch (elements[1].toLowerCase()) {
                    case "pink":
                        bossBar.setColor(BossBar.Color.PINK);
                        break;
                    case "blue":
                        bossBar.setColor(BossBar.Color.BLUE);
                        break;
                    case "red":
                        bossBar.setColor(BossBar.Color.RED);
                        break;
                    case "green":
                        bossBar.setColor(BossBar.Color.GREEN);
                        break;
                    case "yellow":
                        bossBar.setColor(BossBar.Color.YELLOW);
                        break;
                    case "purple":
                        bossBar.setColor(BossBar.Color.PURPLE);
                        break;
                    case "white":
                        bossBar.setColor(BossBar.Color.WHITE);
                        break;
                }
                break;
            case "division":
                switch (elements[1].toLowerCase()) {
                    case "0":
                        bossBar.setDivision(BossBar.Overlay.PROGRESS);
                        break;
                    case "6":
                        bossBar.setDivision(BossBar.Overlay.NOTCHED_6);
                        break;
                    case "10":
                        bossBar.setDivision(BossBar.Overlay.NOTCHED_10);
                        break;
                    case "12":
                        bossBar.setDivision(BossBar.Overlay.NOTCHED_12);
                        break;
                    case "20":
                        bossBar.setDivision(BossBar.Overlay.NOTCHED_20);
                        break;
                }
        }

        bossBar.update();
    }
}
