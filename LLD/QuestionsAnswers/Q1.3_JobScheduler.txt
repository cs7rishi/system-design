import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Q1.3_JobScheduler {
    public static class Job {
        int id;
        String dest;
        public Job(int id, String dest) { this.id = id; this.dest = dest; }
    }

    private final ExecutorService workers;
    private final Map<String, Semaphore> destinationLimits = new ConcurrentHashMap<>();

    public Q1.3_JobScheduler(int numWorkers, Map<String, Integer> limits) {
        this.workers = Executors.newFixedThreadPool(numWorkers);
        limits.forEach((dest, limit) -> destinationLimits.put(dest, new Semaphore(limit, true)));
    }

    public void schedule(List<Job> jobs) {
        for (Job job : jobs) {
            workers.submit(() -> processJob(job));
        }
    }

    private void processJob(Job job) {
        Semaphore limit = destinationLimits.get(job.dest);
        if (limit == null) return; // Unknown destination

        try {
            limit.acquire(); // Respect destination limit
            System.out.println("Executing Job " + job.id + " for destination " + job.dest);
            Thread.sleep(100); // Simulate work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            limit.release();
        }
    }
    
    public void shutdown() {
        workers.shutdown();
    }
}