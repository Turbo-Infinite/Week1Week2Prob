import java.util.*;

class ParkingSpot {
    String licensePlate;
    long entryTime;
    boolean occupied;

    ParkingSpot() {
        this.occupied = false;
    }
}

class ParkingLot {
    private ParkingSpot[] spots;
    private int capacity;
    private int occupiedCount = 0;
    private int totalProbes = 0;
    private Map<Integer, Integer> hourlyOccupancy = new HashMap<>();

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        spots = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) {
            spots[i] = new ParkingSpot();
        }
    }

    // Hash function: licensePlate → preferred spot
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    // Park vehicle using linear probing
    public String parkVehicle(String licensePlate) {
        int index = hash(licensePlate);
        int probes = 0;

        while (spots[index].occupied) {
            probes++;
            index = (index + 1) % capacity;
        }

        spots[index].licensePlate = licensePlate;
        spots[index].entryTime = System.currentTimeMillis();
        spots[index].occupied = true;
        occupiedCount++;
        totalProbes += probes;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        hourlyOccupancy.put(hour, hourlyOccupancy.getOrDefault(hour, 0) + 1);

        return "Assigned spot #" + index + " (" + probes + " probes)";
    }

    // Exit vehicle
    public String exitVehicle(String licensePlate) {
        int index = hash(licensePlate);

        while (spots[index].occupied && !spots[index].licensePlate.equals(licensePlate)) {
            index = (index + 1) % capacity;
        }

        if (!spots[index].occupied) return "Vehicle not found";

        long durationMs = System.currentTimeMillis() - spots[index].entryTime;
        double hours = durationMs / (1000.0 * 60 * 60);
        double fee = hours * 5.0; // $5 per hour

        spots[index].occupied = false;
        occupiedCount--;

        return "Spot #" + index + " freed, Duration: " +
                String.format("%.2f", hours) + "h, Fee: $" + String.format("%.2f", fee);
    }

    // Statistics
    public void getStatistics() {
        double occupancy = (occupiedCount * 100.0) / capacity;
        double avgProbes = (occupiedCount == 0) ? 0 : (totalProbes * 1.0 / occupiedCount);

        int peakHour = hourlyOccupancy.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);

        System.out.println("Occupancy: " + String.format("%.1f", occupancy) + "%");
        System.out.println("Avg Probes: " + String.format("%.2f", avgProbes));
        System.out.println("Peak Hour: " + (peakHour == -1 ? "N/A" : peakHour + ":00"));
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        ParkingLot lot = new ParkingLot(500);

        System.out.println(lot.parkVehicle("ABC-1234"));
        System.out.println(lot.parkVehicle("ABC-1235"));
        System.out.println(lot.parkVehicle("XYZ-9999"));

        Thread.sleep(2000); // simulate time
        System.out.println(lot.exitVehicle("ABC-1234"));

        lot.getStatistics();
    }
}
