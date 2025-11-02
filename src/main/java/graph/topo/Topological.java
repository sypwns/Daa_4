package graph.topo;


import graph.common.Graph;
import graph.common.Metrics;


import java.util.*;


/**
 * Topological ordering for a (condensation) DAG using Kahn's algorithm.
 */
public class Topological {
    private final Graph dag;
    private final Metrics m;


    public Topological(Graph dag, Metrics m) {
        this.dag = dag; this.m = m;
    }


    public Optional<List<Integer>> kahnOrder() {
        int n = dag.n;
        int[] indeg = new int[n];
        for (int u = 0; u < n; u++) for (Graph.Edge e : dag.adj.get(u)) indeg[e.to]++;
        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) { q.add(i); m.kahnPushes++; }
        List<Integer> order = new ArrayList<>();
        m.startTimer();
        while (!q.isEmpty()) {
            int u = q.remove(); m.kahnPops++;
            order.add(u);
            for (Graph.Edge e : dag.adj.get(u)) {
                indeg[e.to]--;
                if (indeg[e.to] == 0) { q.add(e.to); m.kahnPushes++; }
            }
        }
        m.stopTimer();
        if (order.size() != n) return Optional.empty();
        return Optional.of(order);
    }
}