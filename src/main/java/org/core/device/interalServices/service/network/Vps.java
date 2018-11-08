package org.core.device.interalServices.service.network;

import org.core.device.config.ManualConfig;
import org.core.device.interalServices.service.base.AbstractInternalService;
import org.core.device.interalServices.ServiceUids;
import org.core.device.network.NetUtils;
import org.core.device.utils.Utils;

import java.util.List;

/**
 * Прксирование через VPS с выходом на VPS через доступный канал
 * <p>
 * Created by jane on 20.01.17.
 */
public class Vps extends AbstractInternalService {
    @Override
    public long getId() {
        return ServiceUids.VPS;
    }

    @Override
    public boolean internalStart() {
        if ((ManualConfig.getSettings().vpsServerIp != null) && (!ManualConfig.getSettings().vpsServerIp.isEmpty()) &&
                // Если он успешно поднялся пробуем прощупать наш сервер
                (NetUtils.ping(ManualConfig.getSettings().vpsServerIp))) {
            // всё вроде в норме. запускаем режим
            String rslt = Utils.executeShellCommand("/etc/init.d/forwardPorts start " +
                    ManualConfig.getSettings().vpsServerIp + " " +
                    ManualConfig.getSettings().vpsSshPort);
            if (!rslt.isEmpty()) {
                // что-то незаладилось
                internalStop();
            } else {
                // всё в норме
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean internalStop() {
        Utils.executeShellCommand("/etc/init.d/forwardPorts stop");
        return true;
    }

    @Override
    public List<Long> getRequiredServices() {
        return null;
    }

    @Override
    public List<Long> getConflictService() {
        return null;
    }

}
