import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Parking_Lot {

    enum SpotStatus { EMPTY, OCCUPIED, DELETED }

    static class ParkingSpot {
        String licensePlate;
        LocalDateTime entryTime;
        SpotStatus status;

        ParkingSpot() {
            status = SpotStatus.EMPTY;
        }
    }

    private ParkingSpot[] spots;
    private int capacity;
    private int occupiedCount = 0;
    private int totalProbes = 0;
    private Map<Integer, Integer> hourlyOccupancy = new HashMap<>(); // Hour -> count

    public Parking_Lot(int capacity) {
        this.capacity = capacity;
        spots = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) spots[i] = new ParkingSpot();
    }

    private int hashLicensePlate(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    // Park vehicle, return assigned spot
    public synchronized int parkVehicle(String licensePlate) {
        int preferred = hashLicensePlate(licensePlate);
        int probes = 0;

        for (int i = 0; i < capacity; i++) {
            int spotIndex = (preferred + i) % capacity;
            if (spots[spotIndex].status == SpotStatus.EMPTY || spots[spotIndex].status == SpotStatus.DELETED) {
                spots[spotIndex].licensePlate = licensePlate;
                spots[spotIndex].entryTime = LocalDateTime.now();
                spots[spotIndex].status = SpotStatus.OCCUPIED;
                occupiedCount++;
                totalProbes += probes;

                // Track occupancy by hour
                int hour = spots[spotIndex].entryTime.getHour();
                hourlyOccupancy.put(hour, hourlyOccupancy.getOrDefault(hour, 0) + 1);

                System.out.println("Assigned spot #" + spotIndex + " (" + probes + " probes)");
                return spotIndex;
            }
            probes++;
        }
        System.out.println("Parking Full! Cannot assign spot.");
        return -1;
    }

    // Exit vehicle, calculate duration and fee
    public synchronized double exitVehicle(String licensePlate) {
        for (int i = 0; i < capacity; i++) {
            if (spots[i].status == SpotStatus.OCCUPIED && licensePlate.equals(spots[i].licensePlate)) {
                LocalDateTime exitTime = LocalDateTime.now();
                Duration duration = Duration.between(spots[i].entryTime, exitTime);
                double hours = duration.toMinutes() / 60.0;
                double fee = Math.ceil(hours) * 5.0; // $5 per hour

                spots[i].status = SpotStatus.DELETED;
                spots[i].licensePlate = null;
                spots[i].entryTime = null;
                occupiedCount--;

                System.out.printf("Spot #%d freed, Duration: %.2f hours, Fee: $%.2f%n", i, hours, fee);
                return fee;
            }
        }
        System.out.println("Vehicle not found!");
        return 0.0;
    }

    // Find nearest available spot to entrance (lowest index)
    public int nearestAvailableSpot() {
        for (int i = 0; i < capacity; i++) {
            if (spots[i].status == SpotStatus.EMPTY || spots[i].status == SpotStatus.DELETED) return i;
        }
        return -1;
    }

    // Generate statistics
    public void getStatistics() {
        double occupancyPercent = (occupiedCount * 100.0) / capacity;
        double avgProbes = occupiedCount == 0 ? 0 : (totalProbes * 1.0) / occupiedCount;

        int peakHour = -1;
        int maxCount = 0;
        for (Map.Entry<Integer, Integer> entry : hourlyOccupancy.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                peakHour = entry.getKey();
            }
        }

        System.out.printf("Occupancy: %.2f%%, Avg Probes: %.2f, Peak Hour: %d:00-%d:00%n",
                occupancyPercent, avgProbes, peakHour, peakHour+1);
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        Parking_Lot lot = new Parking_Lot(500);

        lot.parkVehicle("ABC-1234");
        lot.parkVehicle("ABC-1235");
        lot.parkVehicle("XYZ-9999");

        Thread.sleep(2000); // Simulate time

        lot.exitVehicle("ABC-1234");

        lot.getStatistics();

        System.out.println("Nearest available spot: " + lot.nearestAvailableSpot());
    }
}