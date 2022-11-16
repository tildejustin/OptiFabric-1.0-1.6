# OptiFabric Origins (1.14.4 & 1.15.2) [![GitHub Releases](https://img.shields.io/github/downloads/Sjouwer/Optifabric-1.14.4-Updated/total)](https://github.com/Sjouwer/OptiFabric-1.14.4-Updated/releases)
 
These are the old versions of 1.14.4 and 1.15.2 OptiFabric, updated to work again with the latest Fabric Loader and latest supported Fabric API. I've used code and fixes from later versions of the [official OptiFabric](https://github.com/Chocohead/OptiFabric) to patch these older ones. Full credit goes to [modmuss50](https://github.com/modmuss50) for creating OptiFabric and [Chocohead](https://github.com/Chocohead) for maintaining it, without them this wouldn't have been possible.
 
## Important:

- This project is not related or supported by either Fabric or OptiFine.
- This project does not contain OptiFine, you must download it separately!

## Installing:
 
After installing Fabric for 1.14.4 or 1.15.2, you will need to place the corresponding OptiFabric Origins mod jar as well as the latest OptiFine jar from the official OptiFine website into your mods folder. Fabric Loader should be version 0.9 or later.

## Links

**[Optifine Download](https://optifine.net/downloads)**

**[OptiFabric Origins Downloads](https://www.curseforge.com/minecraft/mc-mods/optifabric-origins)** (mc 1.14 & 1.15)

**[Official OptiFabric Downloads](https://www.curseforge.com/minecraft/mc-mods/optifabric)** (mc 1.16 and later)

**[Legacy OptiFabric Downloads](https://github.com/RedLime/OptiFabric-Pre1.14/releases)** (mc 1.12 and earlier)

## Issues

If you happen to find an issue and you believe it is to do with OptiFabric Origins and not the official OptiFabric, OptiFine or another mod please open an issue [here](https://github.com/Sjouwer/OptiFabric/issues) 

## Screenshots

![](https://ss.modmuss50.me/javaw_2019-05-22_20-36-25.jpg)

![](https://ss.modmuss50.me/javaw_2019-05-22_19-49-41.jpg)

## How it works

This would not have been possible without Chocohead's [Fabric-ASM](https://github.com/Chocohead/Fabric-ASM).

1. The mod looks for an optifine installer or mod jar in the current mods folder
2. If it finds an installer jar it runs the extract task in its own throwaway classloader.
3. The optifine mod jar is a set of classes that need to replace the ones that minecraft provides.
4. Optifine's replacement classes change the name of some lambada methods, so I take a good guess at the old name (using the original minecraft jar).
5. Remap optifine to intermediary (or yarn in development)
6. Move the patched classes out as they wont do much good on the classpath twice
7. Add optifine to the classpath
8. Register the patching tweaker for every class that needs replacing
9. Replace the target class with the class that was extracted, also do some more fixes to it, and make it public (due to access issues).
10. Hope it works
