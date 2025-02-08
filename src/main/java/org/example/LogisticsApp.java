package org.example;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class LogisticsApp {
    private static final int FIXED_PRICE_PER_KM = 1;

    public static void main(String[] args) throws IOException, InterruptedException {
        // Sample data
        List<String> data = List.of(
                "Apahida,15,100,2017-09-01",
                "Apahida,15,150,2017-09-01",
                "Floresti,7,100,2017-09-01",
                "Turda,29,120,2017-09-01",
                "Apahida,15,199,2017-09-02",
                "Floresti,7,549,2017-09-02",
                "Turda,29,199,2017-09-02"
        );

        List<Package> packages = loadPackagesFromData(data);

        // Group packages by location and delivery date
        Map<String, Map<LocalDate, List<Package>>> groupedPackages = packages.stream()
                .collect(Collectors.groupingBy(Package::getTargetLocation,
                        Collectors.groupingBy(Package::getDeliveryDate)));

        ExecutorService executor = Executors.newFixedThreadPool(groupedPackages.size());
        List<Future<?>> futures = new ArrayList<>();

        int[] totalValue = {0};
        int[] totalRevenue = {0};

        for (String location : groupedPackages.keySet()) {
            for (LocalDate date : groupedPackages.get(location).keySet()) {
                List<Package> group = groupedPackages.get(location).get(date);

                futures.add(executor.submit(() -> {
                    int groupValue = group.stream().mapToInt(Package::getValue).sum();
                    int distance = group.get(0).getTargetDistance();
                    int groupRevenue = distance * FIXED_PRICE_PER_KM;

                    synchronized (totalValue) {
                        totalValue[0] += groupValue;
                        totalRevenue[0] += groupRevenue;
                    }

                    System.out.printf("--------------------------------------------------\n");
                    System.out.printf("[Delivering for <%s> and date <%s> in <%d> seconds]\n", location, date, distance);
                    System.out.printf("--------------------------------------------------\n");

                    try {
                        Thread.sleep(distance * 1000L); // Simulate delivery time
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    return null;
                }));
            }
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        System.out.println("--------------------------------------------------");
        System.out.printf("Total value of all delivered packages: %d\n", totalValue[0]);
        System.out.printf("Total revenue from all deliveries: %d\n", totalRevenue[0]);
        System.out.println("--------------------------------------------------");
    }

    private static List<Package> loadPackagesFromData(List<String> data) {
        return data.stream()
                .map(line -> {
                    String[] parts = line.split(",");
                    String location = parts[0];
                    int distance = Integer.parseInt(parts[1].trim());
                    int value = Integer.parseInt(parts[2].trim());
                    LocalDate date = LocalDate.parse(parts[3].trim());
                    return new Package(location, distance, value, date);
                })
                .collect(Collectors.toList());
    }
}
