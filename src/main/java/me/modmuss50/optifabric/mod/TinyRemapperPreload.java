package me.modmuss50.optifabric.mod;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Arrays;


// note: if updating tiny remapper, make sure to regen this, or it will likely deadlock
// unzip jar and run the following script in the base directory
/*
import os
result = []
for path, subdirs, files in os.walk(os.curdir):
    for name in files:
        if path.startswith("./net") and not path.startswith("./net/fabricmc/tinyremapper/extension"):
            result.append("\"" + os.path.join(path, name).replace("/", ".").removeprefix("..").removesuffix(".class") + "\"")
print(",\n".join(result))
 */
public class TinyRemapperPreload {
    public static void load() {
        // TODO: make this programmatic?
        Arrays.asList(
                "net.fabricmc.tinyremapper.OutputConsumerPath$Builder",
                "net.fabricmc.tinyremapper.BridgeHandler",
                "net.fabricmc.tinyremapper.TinyRemapper",
                "net.fabricmc.tinyremapper.TinyRemapper$Extension",
                "net.fabricmc.tinyremapper.TinyRemapper$ApplyVisitorProvider",
                "net.fabricmc.tinyremapper.NonClassCopyMode",
                "net.fabricmc.tinyremapper.TinyRemapper$AnalyzeVisitorProvider",
                "net.fabricmc.tinyremapper.TinyUtils$MappingAdapter",
                "net.fabricmc.tinyremapper.IMappingProvider$Member",
                "net.fabricmc.tinyremapper.MetaInfRemover",
                "net.fabricmc.tinyremapper.AsmClassRemapper$AsmAnnotationRemapper",
                "net.fabricmc.tinyremapper.IMappingProvider$MappingAcceptor",
                "net.fabricmc.tinyremapper.ClassInstance",
                "net.fabricmc.tinyremapper.MetaInfFixer",
                "net.fabricmc.tinyremapper.VisitTrackingClassRemapper$VisitKind",
                "net.fabricmc.tinyremapper.TinyRemapper$MrjState",
                "net.fabricmc.tinyremapper.IMappingProvider",
                "net.fabricmc.tinyremapper.OutputConsumerPath$ResourceRemapper",
                "net.fabricmc.tinyremapper.TinyRemapper$5",
                "net.fabricmc.tinyremapper.TinyUtils",
                "net.fabricmc.tinyremapper.TinyRemapper$Direction",
                "net.fabricmc.tinyremapper.OutputConsumerPath",
                "net.fabricmc.tinyremapper.MemberInstance",
                "net.fabricmc.tinyremapper.TinyRemapper$4",
                "net.fabricmc.tinyremapper.TinyRemapper$Propagation",
                "net.fabricmc.tinyremapper.TinyRemapper$LinkedMethodPropagation",
                "net.fabricmc.tinyremapper.TinyRemapper$Builder",
                "net.fabricmc.tinyremapper.TinyRemapper$StateProcessor",
                "net.fabricmc.tinyremapper.AsmClassRemapper$AsmAnnotationRemapper$AsmArrayAttributeAnnotationRemapper",
                "net.fabricmc.tinyremapper.AsmClassRemapper",
                "net.fabricmc.tinyremapper.TinyRemapper$3",
                "net.fabricmc.tinyremapper.AsmClassRemapper$AsmMethodRemapper",
                "net.fabricmc.tinyremapper.AsmClassRemapper$AsmFieldRemapper",
                "net.fabricmc.tinyremapper.Main",
                "net.fabricmc.tinyremapper.Propagator",
                "net.fabricmc.tinyremapper.TinyRemapper$2",
                "net.fabricmc.tinyremapper.TinyRemapper$1",
                "net.fabricmc.tinyremapper.PackageAccessChecker",
                "net.fabricmc.tinyremapper.FileSystemReference",
                "net.fabricmc.tinyremapper.InputTag",
                "net.fabricmc.tinyremapper.AsmRemapper",
                "net.fabricmc.tinyremapper.TinyRemapper$1$1",
                "net.fabricmc.tinyremapper.AsmClassRemapper$AsmRecordComponentRemapper",
                "net.fabricmc.tinyremapper.OutputConsumerPath$1",
                "net.fabricmc.tinyremapper.VisitTrackingClassRemapper",
                "net.fabricmc.tinyremapper.api.TrField",
                "net.fabricmc.tinyremapper.api.TrMember$MemberType",
                "net.fabricmc.tinyremapper.api.TrEnvironment",
                "net.fabricmc.tinyremapper.api.TrClass",
                "net.fabricmc.tinyremapper.api.TrRemapper",
                "net.fabricmc.tinyremapper.api.TrMethod",
                "net.fabricmc.tinyremapper.api.TrMember"
        ).forEach(clazz -> {
            try {
                Class.forName(clazz, false, FabricLoader.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                System.out.println("failed to find " + clazz);
            }
        });
    }
}
