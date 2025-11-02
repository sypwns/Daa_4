package graph.common;


/**
 * Simple instrumentation counters and timer.
 */
public class Metrics {
    public long timerStart = 0;
    public long timerNanos = 0;


    // SCC counters
    public long dfsVisits = 0;
    public long dfsEdges = 0;


    // Kahn counters
    public long kahnPops = 0;
    public long kahnPushes = 0;


    // DAG-SP counters
    public long relaxations = 0;


    public void startTimer() { timerStart = System.nanoTime(); }
    public void stopTimer() { timerNanos = System.nanoTime() - timerStart; }
    public String timeMs() { return String.format("%.3f ms", timerNanos / 1e6); }
}