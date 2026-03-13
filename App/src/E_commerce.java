import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class E_commerce {

    // Product class
    static class Product {
        AtomicInteger stock;
        Queue<Integer> waitingList;

        Product(int stock) {
            this.stock = new AtomicInteger(stock);
            this.waitingList = new ConcurrentLinkedQueue<>();
        }
    }

    // productId -> Product
    private ConcurrentHashMap<String, Product> inventory = new ConcurrentHashMap<>();

    // Add product
    public void addProduct(String productId, int stock) {
        inventory.put(productId, new Product(stock));
    }


    // Check stock
    public String checkStock(String productId) {

        Product product = inventory.get(productId);

        if (product == null) {
            return "Product not found";
        }

        return product.stock.get() + " units available";
    }

    // Purchase item
    public String purchaseItem(String productId, int userId) {

        Product product = inventory.get(productId);

        if (product == null) {
            return "Product not found";
        }

        while (true) {

            int currentStock = product.stock.get();

            // If stock finished → add to waiting list
            if (currentStock <= 0) {
                product.waitingList.add(userId);
                return "Added to waiting list, position #" + product.waitingList.size();
            }

            // Atomic decrement to avoid overselling
            if (product.stock.compareAndSet(currentStock, currentStock - 1)) {
                return "Success, " + (currentStock - 1) + " units remaining";
            }
        }
    }

    // Get waiting list
    public List<Integer> getWaitingList(String productId) {

        Product product = inventory.get(productId);

        if (product == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(product.waitingList);
    }

    // Main method for testing
    public static void main(String[] args) throws InterruptedException {

        E_commerce manager = new E_commerce();

        manager.addProduct("IPHONE15_256GB", 5);

        System.out.println(manager.checkStock("IPHONE15_256GB"));

        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Simulate multiple users buying simultaneously
        for (int i = 1; i <= 10; i++) {

            int userId = i;

            executor.submit(() -> {
                String result = manager.purchaseItem("IPHONE15_256GB", userId);
                System.out.println("User " + userId + ": " + result);
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("\nWaiting List: " + manager.getWaitingList("IPHONE15_256GB"));
    }
}
