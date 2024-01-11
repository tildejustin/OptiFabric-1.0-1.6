## Legacy Optifabric

Optifabric for Legacy Fabric (1.3-1.13.2) and Ornithe (1.0-1.13.2)

based on the work of [RedLime's OptiFabric-Pre1.14](https://github.com/RedLime/OptiFabric-Pre1.14), a fork of [hYdos's OptiFabric 1.8.9](https://github.com/hYdos/OptiFabric) which
in turn is a derivative of [modmuss' original OptiFabric](https://github.com/modmuss50/OptiFabric)

## installing

this mod requires an optifine jar in the mods folder alongside optifabric. for 1.7.2 and onwards this can be sourced
from [OptiFine's official website](https://optifine.net/downloads), and older versions these can be retrieved either
from [SpeedyCube64's Pre-1.9 Optifine Archive](https://github.com/speedycube64/Complete_OptiFine_Archive_Pre_1.9) or on
the [OptiFine history thread](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1286605-b1-4-1-9-optifine-history).

## how it works

this mod uses [Minecraft-Java-Edition-Speedrunning/fabric-asm](https://github.com/Minecraft-Java-Edition-Speedrunning/fabric-asm), a minor derivative
of [Cat Core's Fabric-ASM/no-guava](https://github.com/thecatcore/Fabric-ASM/tree/no-guava) which itself is an updated version
of [Chocohead's fabric-asm](https://github.com/Chocohead/Fabric-ASM). fabric asm allows more extreme asm modification and the fork used has no dependency on guava or apache
commons, which older minecraft versions don't provide.

### steps

1. the mod looks for an optifine installer or mod jar or zip in the current mods folder
2. if it finds an installer jar it runs the extract task in its own throwaway classloader
3. the optifine mod jar is a set of classes that need to replace the ones that minecraft provides
4. optifine's replacement classes change the name of some lambda methods, so it take a good guess at the old name (using the original minecraft jar)
5. remap optifine to intermediary
6. move the patched classes out as they won't do much good on the classpath twice
7. add optifine to the classpath
8. register the patching tweaker for every class that needs replacing
9. replace the target class with the class that was extracted, also do some more fixes to it, and make it public (due to access issues)
10. hope it works

## notes

- this does not work for any optifine releases for minecraft 1.1 except for the optifine light edition due to some method signature crash ([log](optifabric-hd-1.1-crash.log))
  outside my control
- this mod does not work in dev or named environments, if someone fixes that do make a pull request
- the accessWideners are created based on the output of tiny remapper's checkPackageAccess option, but they cannot ever be 100% complete. if you have a crash that has to do with and invalid access, report it and it may be able to be fixed.

## class excluder

this is a kind of hidden feature that is occasionally useful to remove files when remapping the jar. to do this, it reads the `optifabric-excluded-classes.json` file
in `.minecraft/config/` if it exists, and expects the file to contain a mapping of versions (field names) to an array of removable classes (or textures or anything else, it's just
a filename).
an example file contents to revert the removal of the profiler for 1.3.2 light is provided below. if the optifine version you are trying to make an excluder extry for has already
been loaded before in your instance, delete its folder in the `.optifine` folder in `.minecraft` to force a recache.

```json
{
  "OptiFine_1.3.2_L_B2": [
    "ik.class"
  ]
}
```
