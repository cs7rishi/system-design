import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Q2.3_ContextManager {
    static class RequestContext {
        String correlationId;
        String userId;
        long startTimeMs;

        static RequestContext create(String userId) {
            RequestContext ctx = new RequestContext();
            ctx.correlationId = "req-" + UUID.randomUUID().toString();
            ctx.userId = userId;
            ctx.startTimeMs = System.currentTimeMillis();
            return ctx;
        }

        RequestContext copy() {
            RequestContext ctx = new RequestContext();
            ctx.correlationId = this.correlationId;
            ctx.userId = this.userId;
            ctx.startTimeMs = this.startTimeMs;
            return ctx;
        }
    }

    private static final ThreadLocal<RequestContext> contextHolder = new ThreadLocal<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void setContext(RequestContext ctx) { contextHolder.set(ctx); }
    public RequestContext getContext() { return contextHolder.get(); }
    public void clearContext() { contextHolder.remove(); }

    public void executeAsync(Runnable task) {
        RequestContext currentContext = getContext();
        // Take a snapshot of the context to pass to the new thread
        RequestContext contextToPropagate = currentContext != null ? currentContext.copy() : null;

        executor.submit(() -> {
            if (contextToPropagate != null) {
                setContext(contextToPropagate);
            }
            try {
                task.run();
            } finally {
                clearContext(); // Prevent memory leaks in thread pool
            }
        });
    }
}