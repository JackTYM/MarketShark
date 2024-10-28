# MarketShark

MarketShark is a Java-based Forge mod designed to automate interactions within the Minecraft Hypixel Skyblock Auction House. Built upon the [Forge1.8.9Template](https://github.com/nea89o/Forge1.8.9Template), this mod includes automatic purchasing and other remote-controlled functionalities for users connected to Hypixel, leveraging a backend for additional features.

## Credits

MarketShark was built using [Forge1.8.9Template](https://github.com/nea89o/Forge1.8.9Template), a template repository by nea89o. This template provides the foundational setup for Forge mods targeting Minecraft version 1.8.9. 

Original template features:
- Architectury Loom integration for Forge modding
- DevAuth for Minecraft account authentication
- Customizable Mod ID on template usage
- JDK 1.8 and JDK 17 compatibility for development
- Gradle build system with support for mixins

Special thanks to nea89o for providing a streamlined template for legacy Minecraft modding.

## Features

- Automated item purchasing on Hypixel Skyblock using the CoflNet websocket
- Backend server integration for remote control
- Remote Start/Stop functionality
- Discord bot integration for command and control
- CLI tool for easy configuration (optional)
- AutoList feature for managing in-game listings

## Building MarketShark

### Requirements

To build and run MarketShark, you will need:
- Java 1.8 JDK for the project SDK
- Java 17 JDK for Gradle JVM
- IntelliJ IDEA for development

Download the required JDKs from [Adoptium](https://adoptium.net/temurin/releases).

### Building Instructions

1. **Clone the Repository**
   ```bash
   git clone https://github.com/JackTYM/MarketShark
   cd MarketShark
   ```

2. **Configure IntelliJ IDEA**
   - Set the Gradle JVM to the Java 17 JDK.
   - Set the Project SDK to the Java 1.8 JDK.
   - Synchronize Gradle and ensure the `Minecraft Client` run task appears.

3. **Exporting the Mod**
   - Run the `gradle build` task.
   - The compiled mod can be found at `build/libs/<modid>-<version>.jar`.
   - Ignore the jars in the `build/badjars` folder, as these are intermediary files.

4. **Optional Configuration for Mac Users**
   - Remove the `-XStartOnFirstThread` VM argument in your run configuration if you encounter issues running the client.

## Missing Features

- None

## Known Vulnerabilities

- None reported

## Known Bugs

- Error when rejoining island after limbo.

## Contributing

Contributions are welcome! To contribute:

1. **Fork** the repository.
2. **Clone** your fork: `git clone https://github.com/YOUR_USERNAME/MarketShark.git`
3. **Create a new branch** for your feature or bug fix: `git checkout -b feature-name`
4. **Commit** your changes: `git commit -m "Add new feature"`
5. **Push** to your fork: `git push origin feature-name`
6. **Submit a pull request** with a description of your changes.

Pull requests will be reviewed and accepted if applicable to the project goals.

---

Thank you for your interest in MarketShark!
