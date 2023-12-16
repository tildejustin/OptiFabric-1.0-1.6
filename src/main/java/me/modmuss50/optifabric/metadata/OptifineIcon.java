package me.modmuss50.optifabric.metadata;

import me.modmuss50.optifabric.IOUtils;

import java.io.IOException;

@SuppressWarnings("DataFlowIssue")
public class OptifineIcon {
    public static final String DATA;

    static {
        try {
            DATA = new String(IOUtils.toByteArray(OptifineIcon.class.getResourceAsStream("/assets/optifabric/optifine_icon")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OptifineIcon() {
        throw new UnsupportedOperationException();
    }
}
