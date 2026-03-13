import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Social_Media {

    private ConcurrentHashMap<String, Integer> usernameToUserId = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, AtomicInteger> attemptFrequency = new ConcurrentHashMap<>();

    public boolean checkAvailability(String username) {

        attemptFrequency
                .computeIfAbsent(username, k -> new AtomicInteger(0))
                .incrementAndGet();

        return !usernameToUserId.containsKey(username);
    }

    public boolean registerUsername(String username, int userId) {

        if (usernameToUserId.putIfAbsent(username, userId) == null) {
            return true;
        }

        return false;
    }

    public List<String> suggestAlternatives(String username) {

        List<String> suggestions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;
            if (!usernameToUserId.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        if (username.contains("_")) {
            String dotVersion = username.replace("_", ".");
            if (!usernameToUserId.containsKey(dotVersion)) {
                suggestions.add(dotVersion);
            }
        }

        return suggestions;
    }

    public String getMostAttempted() {

        String mostAttempted = null;
        int max = 0;

        for (Map.Entry<String, AtomicInteger> entry : attemptFrequency.entrySet()) {

            int count = entry.getValue().get();

            if (count > max) {
                max = count;
                mostAttempted = entry.getKey();
            }
        }

        return mostAttempted + " (" + max + " attempts)";
    }

    public static void main(String[] args) {

         Social_Media checker = new Social_Media();

        checker.registerUsername("john_doe", 1);
        checker.registerUsername("admin", 2);

        System.out.println(checker.checkAvailability("john_doe"));
        System.out.println(checker.checkAvailability("jane_smith"));

        System.out.println(checker.suggestAlternatives("john_doe"));

        for (int i = 0; i < 100; i++) {
            checker.checkAvailability("admin");
        }

        System.out.println(checker.getMostAttempted());
    }
}