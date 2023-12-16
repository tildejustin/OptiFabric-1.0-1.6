package me.modmuss50.optifabric.metadata;

import net.fabricmc.loader.api.metadata.ContactInformation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class OptifineContactInformation implements ContactInformation {
    static final OptifineContactInformation INSTANCE = new OptifineContactInformation();
    private static final Map<String, String> OPTIFINE_CONTACTS = new HashMap<>();

    static {
        OPTIFINE_CONTACTS.put("email", "optifinex@gmail.com");
        OPTIFINE_CONTACTS.put("homepage", "https://optifine.net");
        OPTIFINE_CONTACTS.put("issues", "https://github.com/sp614x/optifine/issues");
        OPTIFINE_CONTACTS.put("sources", "https://github.com/sp614x/optifine");
    }

    private OptifineContactInformation() {
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(OPTIFINE_CONTACTS.get(key));
    }

    @Override
    public Map<String, String> asMap() {
        return OPTIFINE_CONTACTS;
    }
}
