import java.util.*;

class VideoData {
    String videoId;
    String content; // In real system: metadata or pointer to video file
    public VideoData(String videoId, String content) {
        this.videoId = videoId;
        this.content = content;
    }
}

public class Multi_Level {

    // L1: In-memory cache (LRU)
    private LinkedHashMap<String, VideoData> L1;
    private int L1Capacity = 10000;

    // L2: SSD-backed simulation (LRU + access counts)
    private LinkedHashMap<String, VideoData> L2;
    private int L2Capacity = 100000;
    private Map<String, Integer> L2AccessCount = new HashMap<>();
    private int promoteThreshold = 3;

    // L3: Database (slow)
    private Map<String, VideoData> L3;

    // Hit statistics
    private long L1Hits = 0, L1Requests = 0;
    private long L2Hits = 0, L2Requests = 0;
    private long L3Hits = 0, L3Requests = 0;

    public Multi_Level(Map<String, VideoData> database) {
        // LRU cache: accessOrder = true
        L1 = new LinkedHashMap<>(L1Capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                return size() > L1Capacity;
            }
        };
        L2 = new LinkedHashMap<>(L2Capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                return size() > L2Capacity;
            }
        };
        L3 = database;
    }

    // Fetch video
    public VideoData getVideo(String videoId) {
        L1Requests++;
        if (L1.containsKey(videoId)) {
            L1Hits++;
            System.out.println(videoId + " → L1 Cache HIT (~0.5ms)");
            return L1.get(videoId);
        }

        L2Requests++;
        if (L2.containsKey(videoId)) {
            L2Hits++;
            System.out.println(videoId + " → L1 Cache MISS, L2 Cache HIT (~5ms)");
            // Promote to L1 if access count exceeds threshold
            int count = L2AccessCount.getOrDefault(videoId, 0) + 1;
            L2AccessCount.put(videoId, count);
            if (count >= promoteThreshold) {
                L1.put(videoId, L2.get(videoId));
                System.out.println("Promoted " + videoId + " to L1 cache");
            }
            return L2.get(videoId);
        }

        L3Requests++;
        if (L3.containsKey(videoId)) {
            L3Hits++;
            System.out.println(videoId + " → L1 & L2 Cache MISS, L3 Database HIT (~150ms)");
            VideoData video = L3.get(videoId);
            // Add to L2
            L2.put(videoId, video);
            L2AccessCount.put(videoId, 1);
            return video;
        }

        System.out.println(videoId + " → Video NOT FOUND");
        return null;
    }

    // Invalidate video
    public void invalidate(String videoId) {
        L1.remove(videoId);
        L2.remove(videoId);
        L2AccessCount.remove(videoId);
        System.out.println(videoId + " invalidated from caches");
    }

    // Display cache statistics
    public void getStatistics() {
        long totalRequests = L1Requests + L2Requests + L3Requests;
        long totalHits = L1Hits + L2Hits + L3Hits;
        double L1HitRate = L1Requests == 0 ? 0 : (L1Hits * 100.0 / L1Requests);
        double L2HitRate = L2Requests == 0 ? 0 : (L2Hits * 100.0 / L2Requests);
        double L3HitRate = L3Requests == 0 ? 0 : (L3Hits * 100.0 / L3Requests);
        double overallHitRate = totalRequests == 0 ? 0 : (totalHits * 100.0 / totalRequests);

        System.out.printf("L1: Hit Rate %.2f%%, Avg Time: 0.5ms%n", L1HitRate);
        System.out.printf("L2: Hit Rate %.2f%%, Avg Time: 5ms%n", L2HitRate);
        System.out.printf("L3: Hit Rate %.2f%%, Avg Time: 150ms%n", L3HitRate);
        System.out.printf("Overall Hit Rate: %.2f%%, Avg Time: %.1fms%n", overallHitRate,
                (L1Hits*0.5 + L2Hits*5 + L3Hits*150)/Math.max(totalHits, 1.0));
    }

    // Demo
    public static void main(String[] args) {
        // Simulated database
        Map<String, VideoData> database = new HashMap<>();
        for (int i = 1; i <= 1000; i++) {
            database.put("video_" + i, new VideoData("video_" + i, "Content of video " + i));
        }

        Multi_Level cache = new Multi_Level(database);

        // Simulate access patterns
        cache.getVideo("video_123");
        cache.getVideo("video_123");
        cache.getVideo("video_123");
        cache.getVideo("video_999");
        cache.getVideo("video_999");
        cache.getVideo("video_500");

        cache.getStatistics();
    }
}