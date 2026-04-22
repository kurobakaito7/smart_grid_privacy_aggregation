package com.smartgrid.meter;

import com.smartgrid.entity.Device;
import com.smartgrid.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class MeterManager {

    @Autowired
    private DeviceRepository deviceRepository;

    @Value("${smartgrid.meter.count}")
    private int meterCount;

    @Value("${smartgrid.report.interval}")
    private long reportInterval;

    private final Map<String, MeterSimulator> meters = new ConcurrentHashMap<>();
    private final Map<String, AtomicBoolean> meterStatus = new ConcurrentHashMap<>();
    private final Map<String, Thread> meterThreads = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void initialize() {
        for (int i = 1; i <= meterCount; i++) {
            String deviceId = String.format("DEVICE%03d", i);
            Device device = deviceRepository.findById(deviceId).orElse(null);

            if (device == null) {
                device = new Device();
                device.setDeviceId(deviceId);
                device.setStatus("stopped");
                device.setLastSeq(0);
                device.setPublicKey("PENDING");
                deviceRepository.save(device);
            }

            meterStatus.put(deviceId, new AtomicBoolean(false));
        }
    }

    public void startMeter(String deviceId) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }

        if (meters.containsKey(deviceId)) {
            return;
        }

        MeterSimulator meter = new MeterSimulator(deviceId, null);
        meters.put(deviceId, meter);

        device.setStatus("running");
        device.setPublicKey(meter.getPublicKey());
        deviceRepository.save(device);

        meterStatus.get(deviceId).set(true);

        Thread reportThread = new Thread(() -> {
            long randomDelay = new Random().nextInt(30000);
            try {
                Thread.sleep(randomDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            while (meterStatus.get(deviceId).get()) {
                try {
                    reportData(deviceId);
                    Thread.sleep(reportInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error reporting data for " + deviceId + ": " + e.getMessage());
                }
            }
        });

        meterThreads.put(deviceId, reportThread);
        reportThread.start();
    }

    public void stopMeter(String deviceId) {
        meterStatus.get(deviceId).set(false);

        Thread thread = meterThreads.remove(deviceId);
        if (thread != null) {
            thread.interrupt();
        }

        meters.remove(deviceId);

        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device != null) {
            device.setStatus("stopped");
            deviceRepository.save(device);
        }
    }

    public void startAllMeters() {
        for (String deviceId : meterStatus.keySet()) {
            if (!meterStatus.get(deviceId).get()) {
                startMeter(deviceId);
            }
        }
    }

    public void stopAllMeters() {
        for (String deviceId : new ArrayList<>(meterStatus.keySet())) {
            stopMeter(deviceId);
        }
    }

    private void reportData(String deviceId) {
        MeterSimulator meter = meters.get(deviceId);
        if (meter == null) {
            return;
        }

        long timestamp = System.currentTimeMillis();
        int[] data = meter.generateRandomData();
        String[] encrypted = meter.encryptAndSign(data, timestamp);

        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device != null) {
            device.setLastReportTime(java.time.LocalDateTime.now());
            device.setLastSeq(meter.getNextSequence());
            deviceRepository.save(device);
        }

        System.out.println(String.format("[%s] Reported: voltage=%d, current=%d, power=%d",
                deviceId, data[0], data[1], data[2]));
    }

    public Map<String, Object> getMeterStatus(String deviceId) {
        Map<String, Object> status = new HashMap<>();
        Device device = deviceRepository.findById(deviceId).orElse(null);

        if (device != null) {
            status.put("deviceId", deviceId);
            status.put("status", device.getStatus());
            status.put("lastReportTime", device.getLastReportTime());
            status.put("lastSeq", device.getLastSeq());
            status.put("publicKey", device.getPublicKey());
        }

        return status;
    }

    public List<Map<String, Object>> getAllMeterStatus() {
        List<Map<String, Object>> statuses = new ArrayList<>();
        for (String deviceId : meterStatus.keySet()) {
            statuses.add(getMeterStatus(deviceId));
        }
        return statuses;
    }
}
