package com.genyo.core.stats;

import com.genyo.Genyo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ClientIdManager {

    private static final String CLIENT_ID_FILE = "genyo_id.txt";

    public static String getClientId(Path configDir) {
        try {
            Path file = configDir.resolve(CLIENT_ID_FILE);

            if (Files.exists(file)) {
                return Files.readString(file).trim();
            } else {
                String id = UUID.randomUUID().toString();
                Files.writeString(file, id);
                return id;
            }
        } catch (IOException e) {
            Genyo.LOG.error(e.getMessage());

            return UUID.randomUUID().toString();
        }
    }

}
