package Utils;

import java.util.concurrent.ThreadLocalRandom;

public class UserSessionUtils {
    public static Integer sessionId = 1;

    public static String generateRandomIP() {
        return String.format("%d.%d.%d.%d",
                ThreadLocalRandom.current().nextInt(0, 256),
                ThreadLocalRandom.current().nextInt(0, 256),
                ThreadLocalRandom.current().nextInt(0, 256),
                ThreadLocalRandom.current().nextInt(0, 256)
        );
    }
}
