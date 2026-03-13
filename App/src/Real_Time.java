import java.util.*;
import java.util.concurrent.*;

public class Real_Time {

    // pageUrl -> visit count
    private Map<String, Integer> pageViews = new ConcurrentHashMap<>();

    // pageUrl -> unique visitors
    private Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    // traffic source -> count
    private Map<String, Integer> trafficSources = new ConcurrentHashMap<>();

    // Process page view event
    public void processEvent(String url, String userId, String source) {

        // update page views
        pageViews.put(url, pageViews.getOrDefault(url, 0) + 1);

        // update unique visitors
        uniqueVisitors
                .computeIfAbsent(url, k -> ConcurrentHashMap.newKeySet())
                .add(userId);

        // update traffic source
        trafficSources.put(source, trafficSources.getOrDefault(source, 0) + 1);
    }

    // Get Top 10 pages
    private List<Map.Entry<String, Integer>> getTopPages() {

        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {

            pq.offer(entry);

            if (pq.size() > 10) {
                pq.poll();
            }
        }

        List<Map.Entry<String, Integer>> result = new ArrayList<>(pq);

        result.sort((a, b) -> b.getValue() - a.getValue());

        return result;
    }

    // Display dashboard
    public void getDashboard() {

        System.out.println("\n===== REAL TIME DASHBOARD =====");

        System.out.println("\nTop Pages:");

        List<Map.Entry<String, Integer>> topPages = getTopPages();

        int rank = 1;

        for (Map.Entry<String, Integer> entry : topPages) {

            String page = entry.getKey();
            int views = entry.getValue();

            int unique = uniqueVisitors.getOrDefault(page, new HashSet<>()).size();

            System.out.println(
                    rank + ". " + page +
                            " - " + views +
                            " views (" + unique + " unique)"
            );

            rank++;
        }

        System.out.println("\nTraffic Sources:");

        int total = trafficSources.values().stream().mapToInt(i -> i).sum();

        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {

            double percent = (entry.getValue() * 100.0) / total;

            System.out.printf("%s: %.2f%%\n", entry.getKey(), percent);
        }
    }

    // Start dashboard refresh every 5 seconds
    public void startDashboard() {

        ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            getDashboard();
        }, 5, 5, TimeUnit.SECONDS);
    }

    // Demo simulation
    public static void main(String[] args) throws InterruptedException {

        Real_Time analytics = new Real_Time();

        analytics.startDashboard();

        String[] pages = {
                "/article/breaking-news",
                "/sports/championship",
                "/tech/ai-future",
                "/world/economy"
        };

        String[] sources = {
                "google",
                "facebook",
                "direct",
                "twitter"
        };

        Random rand = new Random();

        // simulate incoming traffic
        for (int i = 0; i < 200; i++) {

            String url = pages[rand.nextInt(pages.length)];
            String user = "user_" + rand.nextInt(100);
            String source = sources[rand.nextInt(sources.length)];

            analytics.processEvent(url, user, source);

            Thread.sleep(50);
        }
    }
}