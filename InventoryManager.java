import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class InventoryManager {
    private HashMap<String, AtomicInteger> stockMap = new HashMap<>();
    private LinkedHashMap<Integer, String> waitingList = new LinkedHashMap<>();

    // Initialize product stock
    public void addProduct(String productId, int stockCount) {
        stockMap.put(productId, new AtomicInteger(stockCount));
    }

    // Check stock availability
    public int checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);
        return (stock != null) ? stock.get() : 0;
    }

    // Process purchase request
    public synchronized String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockMap.get(productId);

        if (stock == null) return "Product not found";

        if (stock.get() > 0) {
            stock.decrementAndGet();
            return "Success, " + stock.get() + " units remaining";
        } else {
            waitingList.put(userId, productId);
            return "Added to waiting list, position #" + waitingList.size();
        }
    }

    // Display waiting list
    public void showWaitingList() {
        System.out.println("Waiting List: " + waitingList);
    }

    // Demo
    public static void main(String[] args) {
        InventoryManager manager = new InventoryManager();
        manager.addProduct("IPHONE15_256GB", 100);

        System.out.println("checkStock(\"IPHONE15_256GB\") → " + manager.checkStock("IPHONE15_256GB") + " units available");

        System.out.println(manager.purchaseItem("IPHONE15_256GB", 12345)); // Success
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 67890)); // Success

        // Simulate 100 purchases
        for (int i = 0; i < 98; i++) {
            manager.purchaseItem("IPHONE15_256GB", i);
        }

        // Stock exhausted
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 99999)); // Added to waiting list
        manager.showWaitingList();
    }
}
