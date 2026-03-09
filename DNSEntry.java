import java.util.*;
import java.util.concurrent.*;

class DNSEntry {
    String domain;
    String ipAddress;
    long expiryTime; // in milliseconds

    DNSEntry(String domain, String ipAddress, int ttlSeconds) {
        this.domain = domain;
        this.ipAddress = ipAddress;
        this.expiryTime = System.currentTimeMillis() + ttlSeconds * 1000;
    }

    boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

class DNSCache {
    private final int MAX_SIZE;
    private LinkedHashMap<String, DNSEntry> cache;
    private int hits = 0, misses = 0;

    public DNSCache(int maxSize) {
        this.MAX_SIZE = maxSize;
        this.cache = new LinkedHashMap<>(maxSize, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > MAX_SIZE;
            }
        };

        // Background thread to clean expired entries
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            synchronized (cache) {
                Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();
                while (it.hasNext()) {
                    if (it.next().getValue().isExpired()) {
                        it.remove();
                    }
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    // Resolve domain
    public synchronized String resolve(String domain) {
        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits++;
            return "Cache HIT → " + entry.ipAddress;
        } else {
            misses++;
            // Simulate upstream DNS query
            String newIp = queryUpstream(domain);
            cache.put(domain, new DNSEntry(domain, newIp, 300)); // TTL = 300s
            return "Cache MISS → Query upstream → " + newIp;
        }
    }

    // Simulated upstream DNS query
    private String queryUpstream(String domain) {
        return "172.217." + new Random().nextInt(255) + "." + new Random().nextInt(255);
    }

    // Stats
    public void getCacheStats() {
        int total = hits + misses;
        double hitRate = (total == 0) ? 0 : (hits * 100.0 / total);
        System.out.println("Hit Rate: " + hitRate + "%, Hits=" + hits + ", Misses=" + misses);
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        DNSCache dnsCache = new DNSCache(5);

        System.out.println(dnsCache.resolve("google.com")); // MISS
        System.out.println(dnsCache.resolve("google.com")); // HIT

        Thread.sleep(310 * 1000); // wait for TTL expiry
        System.out.println(dnsCache.resolve("google.com")); // EXPIRED → MISS

        dnsCache.getCacheStats();
    }
}
