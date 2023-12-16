package me.modmuss50.optifabric.metadata;

import net.fabricmc.loader.api.metadata.ModOrigin;

public interface OptifineOrigin extends ModOrigin {
    @Override
    default Kind getKind() {
        return Kind.UNKNOWN;
    }

    @Override
    default String getParentModId() {
        return "optifabric";
    }

    @Override
    default String getParentSubLocation() {
        throw new UnsupportedOperationException("kind " + getKind().name() + " doesn't have a parent sub-location");
    }
}
