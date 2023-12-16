package me.modmuss50.optifabric.metadata;

import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.Person;

public class OptifinePerson implements Person {
    static final OptifinePerson INSTANCE = new OptifinePerson();

    private OptifinePerson() {
    }

    @Override
    public String getName() {
        return "sp614x";
    }

    @Override
    public ContactInformation getContact() {
        return ContactInformation.EMPTY;
    }
}
