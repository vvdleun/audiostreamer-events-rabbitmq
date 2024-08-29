package nl.vincentvanderleun.bluos.service;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import nl.vincentvanderleun.bluos.service.impl.LsdpUdpPollerRunnable;

/**
 * This class ensures there is up to 1 worker that waits for incoming LSDP messages 
 * that are broadcast on the network (UDP) and calls the specified consumer on each received
 * LSDP Announcement message.
 */
public class BluOsStreamerDetectionService {
    private static final LsdpUdpPollerRunnable RUNNABLE_INSTANCE = new LsdpUdpPollerRunnable();

    private static ExecutorService executorService = null;
    private static Future<?> workerFuture = null;

    public BluOsStreamerDetectionService() {
    }

    public void startAndWaitUntilFinished() {
        try {
            start();
            workerFuture.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public synchronized void start() {
        System.out.println("Starting worker...");
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        if (workerFuture == null || workerFuture.isDone()) {
            workerFuture = executorService.submit(RUNNABLE_INSTANCE);
        } else {
            throw new IllegalStateException("Worker is already running");
        }
    }

    public synchronized void stop() {
        System.out.println("Stopping worker...");
        RUNNABLE_INSTANCE.stop();

        workerFuture.cancel(true);
        executorService.shutdown();
    }
}