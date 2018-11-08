package org.core.device.os;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * используется запущенный скрипт shutdowner.sh (см. webapp/shell)
 * <p>
 * <p>
 * запуск настроен в rc.local
 * <p>
 * sudo /etc/init.d/shutdowner > stdoutfile 2> stderrfile &
 * <p>
 * Created by jane on 13.01.17.
 */
public class Shutdown {
    public static final String shutdownFile = "/opt/shutdown";

    public void doShutdown() {
        File file = new File(shutdownFile);
        try (FileOutputStream fis = new FileOutputStream(file)) {
            fis.write(2);
        } catch (IOException fe) {
            throw new RuntimeException(fe);
        }
    }
}