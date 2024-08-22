![Image](/files/SharkByte_Logo.png)

# sharkbyte-bossbar

This is a platform-independent system that allows boss bars to be created easily. It requires
[PacketEvents](https://github.com/retrooper/packetevents) to function, which can be downloaded as a plugin on their
[Modrinth page](https://modrinth.com/plugin/packetevents).

Features:
- Unlimited characters.
- Full control over bar color (1.9+).
- Full control over bar division mode (1.9+).
- Simple and lightweight.

NOTE: This project will primarily be maintained for 1.9+. Versions beneath the BossBar packet addition may not
receive support.

More information about each module can be found inside their respective folders.

# self promo

If you are using sharkbyte-bossbar on the Spigot platform, I would highly recommend you include
[BetterReload](https://github.com/amnoah/BetterReload) compatibility.

BetterReload adds a universal reload event, replacing the traditional /reload command. This event is passed to plugins,
allowing them to handle a reload as they see fit. Your plugin could use this event to cycle through all of your
BossBar objects and update the text on them from your configuration.

BetterReload also allows for users to individually reload plugins, removing the need for a reload command to be built
into every plugin.

# how to use

You can add sharkbyte-bossbar to your project using [JitPack](https://jitpack.io/#amnoah/sharkbyte-bossbar/).
Select the dependency system you're using and copy the repository/dependency settings into your project. From there,
just reload your dependencies and you should have sharkbyte-bossbar accessible from your project.

# support

For general support, please join my [Discord server](https://discord.gg/ey9uTg3hcy).

For issues with the project, please open an issue in the issues tab.
