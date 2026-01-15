package com.jptechgenius.payroll.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * [System Health Controller]
 * --------------------------
 * Ei controller-tar kaj holo Frontend (Sidebar Modal) e server er current status pathano.
 * Jemon: Server kotoilokkhon dhore cholche, RAM koto khacche, Database connect ache kina etc.
 * * Note: Eta kono HTML page return kore na, sorasori JSON data return kore (REST API).
 * JavaScript (fetch API) er maddhome sidebar theke eta call kora hoy.
 */
@RestController // HTML noy, JSON Data return korbe tai @RestController
public class SystemHealthController {

    // Database connection check korar jonno DataSource object lagbe
    private final DataSource dataSource;

    public SystemHealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/system-health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        // Ekta Map banacchi jekhane sob data (key-value pair) rakhbo
        Map<String, Object> status = new HashMap<>();
        // Doshomik sonkha sundor kore dekhanor jonno format (e.g. 10.55 MB)
        DecimalFormat df = new DecimalFormat("#.##");

        // ==========================================
        // 1. UPTIME CALCULATION (সার্ভার কতক্ষণ চলছে)
        // ==========================================
        // Server start howar por theke koto millisecond par hoyeche ta nilam
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();

        // Millisecond theke Ghonta (Hours) ar Minit (Minutes) e convert korlam
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60;

        // Map e put korlam (Ex: "2h 30m")
        status.put("uptime", hours + "h " + minutes + "m");

        // ==========================================
        // 2. DATABASE CHECK (ডাটাবেস কানেকশন ঠিক আছে কিনা)
        // ==========================================
        long start = System.currentTimeMillis(); // Check shuru korar time
        try (Connection conn = dataSource.getConnection()) {
            // Check korchi connection valid kina (1000ms ba 1 second wait korbe)
            if (conn.isValid(1000)) {
                long latency = System.currentTimeMillis() - start; // Koto somoy laglo ta ber korlam
                status.put("dbStatus", "Online (" + latency + "ms)");
                status.put("dbColor", "success"); // Frontend e Green color dekhabe
            } else {
                status.put("dbStatus", "Unreachable");
                status.put("dbColor", "danger"); // Red color dekhabe
            }
        } catch (Exception e) {
            // Jodi connection nitei na pare (Database bondho thakle)
            status.put("dbStatus", "Connection Failed");
            status.put("dbColor", "danger");
        }

        // ==========================================
        // 3. MEMORY USAGE (RAM কতটুকু ব্যবহার হচ্ছে)
        // ==========================================
        Runtime runtime = Runtime.getRuntime(); // JVM er runtime object

        // Byte theke MB te convert korchi (1024 * 1024 diye vag kore)
        double totalMem = runtime.totalMemory() / (1024.0 * 1024.0);
        double freeMem = runtime.freeMemory() / (1024.0 * 1024.0);
        double usedMem = totalMem - freeMem;

        // Koto percent RAM use hocche ta ber kora
        int memPercent = (int) ((usedMem / totalMem) * 100);

        status.put("memoryUsed", df.format(usedMem) + " MB");
        status.put("memoryTotal", df.format(totalMem) + " MB");
        status.put("memoryPercent", memPercent);

        // ==========================================
        // 4. DISK SPACE (সার্ভারের হার্ডডিস্ক স্পেস)
        // ==========================================
        File root = new File("/"); // Root directory dhorechi

        // Byte theke GB te convert kora hocche
        double totalDisk = root.getTotalSpace() / (1024.0 * 1024.0 * 1024.0);
        double freeDisk = root.getFreeSpace() / (1024.0 * 1024.0 * 1024.0);

        // Koto percent Disk full hoyeche ta ber kora
        int diskPercent = (int) (((totalDisk - freeDisk) / totalDisk) * 100);

        status.put("diskUsed", df.format(totalDisk - freeDisk) + " GB");
        status.put("diskTotal", df.format(totalDisk) + " GB");
        status.put("diskPercent", diskPercent);

        // Finally, sob data JSON akare frontend e pathiye dilam
        return ResponseEntity.ok(status);
    }
}