package nl.vincentvanderleun.bluos;

import nl.vincentvanderleun.bluos.service.BluOsStreamerDetectionService;

public class BluOsPlayersDetectorApp {

    public static void main(String[] args) throws Exception {
        var detectionService = new BluOsStreamerDetectionService();
        detectionService.startAndWaitUntilFinished();
    }
}
