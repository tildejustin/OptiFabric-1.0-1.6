package me.modmuss50.optifabric.metadata;

import me.modmuss50.optifabric.mod.OptifineVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.*;

import java.util.*;

public class OptifineMetadata implements ModMetadata {
    private final Version version;

    OptifineMetadata(Version version) {
        this.version = version;
    }

    @Override
    public String getType() {
        return "mcp";
    }

    @Override
    public String getId() {
        return "optifine";
    }

    @Override
    public Collection<String> getProvides() {
        return new ArrayList<>();
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public ModEnvironment getEnvironment() {
        return ModEnvironment.CLIENT;
    }

    @Override
    public Collection<ModDependency> getDependencies() {
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return OptifineVersion.version;
    }

    @Override
    public String getDescription() {
        return "Chasing the Minecraft Performance. This mod adds support for HD textures and a lot of options for better looks and performance. Doubling the FPS is common.";
    }

    @Override
    public Collection<Person> getAuthors() {
        HashSet<Person> authors = new HashSet<>();
        authors.add(OptifinePerson.INSTANCE);
        return authors;
    }

    @Override
    public Collection<Person> getContributors() {
        HashSet<Person> contributors = new HashSet<>();
        contributors.add(OptifinePerson.INSTANCE);
        return contributors;
    }

    @Override
    public ContactInformation getContact() {
        return OptifineContactInformation.INSTANCE;
    }

    @Override
    public Collection<String> getLicense() {
        HashSet<String> licenses = new HashSet<>();
        licenses.add("All rights reserved");
        return licenses;
    }

    @Override
    public Optional<String> getIconPath(int size) {
        return Optional.of("assets/optifine/icon.png");
    }

    @Override
    public boolean containsCustomValue(String key) {
        return false;
    }

    @Override
    public CustomValue getCustomValue(String key) {
        return null;
    }

    @Override
    public Map<String, CustomValue> getCustomValues() {
        return new HashMap<>();
    }

    @Override
    public boolean containsCustomElement(String key) {
        return false;
    }
}
