
package com.gruppem.percentageservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "energy")
public class EnergyProperties {
    private String rawQueue;     // очередь сырых сообщений (producer/user → percentage)
    private String updateQueue;  // очередь от usage-service
    private String finalQueue;   // куда отправлять финальный процент

    // геттеры/сеттеры
    public String getRawQueue() { return rawQueue; }
    public void setRawQueue(String rawQueue) { this.rawQueue = rawQueue; }

    public String getUpdateQueue() { return updateQueue; }
    public void setUpdateQueue(String updateQueue) { this.updateQueue = updateQueue; }

    public String getFinalQueue() { return finalQueue; }
    public void setFinalQueue(String finalQueue) { this.finalQueue = finalQueue; }
}
