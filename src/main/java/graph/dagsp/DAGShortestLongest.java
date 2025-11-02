package graph.dagsp;

import java.util.*;
import graph.common.Graph;
import graph.common.Metrics;

public class DAGShortestLongest {
    private final Graph dag;
    private final Metrics m;

    public DAGShortestLongest(Graph dag, Metrics m) {
        this.dag = dag;
        this.m = m;
    }

    // -----------------------------
    // shortest path on DAG
    // -----------------------------
    public Result shortestFrom(int src, List<Integer> topoOrder) {
        int n = dag.n;
        double[] dist = new double[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        dist[src] = 0;
        m.startTimer();

        for (int u : topoOrder) {
            if (dist[u] == Double.POSITIVE_INFINITY) continue;
            for (Graph.Edge e : dag.adj.get(u)) {
                m.relaxations++;
                if (dist[e.to] > dist[u] + e.w) {
                    dist[e.to] = dist[u] + e.w;
                    parent[e.to] = u;
                }
            }
        }

        m.stopTimer();
        return new Result(dist, parent);
    }

    // -----------------------------
    // longest path (critical path)
    // -----------------------------
    public Result longestPath(List<Integer> topoOrder) {
        int n = dag.n;
        double NEG_INF = Double.NEGATIVE_INFINITY;
        double[] best = new double[n];
        Arrays.fill(best, NEG_INF);
        int[] parent = new int[n];
        Arrays.fill(parent, -1);

        for (int v : topoOrder) {
            boolean hasIn = false;
            for (int u = 0; u < n; u++) {
                for (Graph.Edge e : dag.adj.get(u))
                    if (e.to == v) { hasIn = true; break; }
                if (hasIn) break;
            }
            if (!hasIn) best[v] = 0;
        }

        m.startTimer();
        for (int v : topoOrder) {
            if (best[v] == NEG_INF) continue;
            for (Graph.Edge e : dag.adj.get(v)) {
                m.relaxations++;
                if (best[e.to] < best[v] + e.w) {
                    best[e.to] = best[v] + e.w;
                    parent[e.to] = v;
                }
            }
        }

        m.stopTimer();
        return new Result(best, parent);
    }

    // -----------------------------
    // reconstruct path helper
    // -----------------------------
    public static List<Integer> reconstruct(int[] parent, int target) {
        if (parent[target] == -1) return Arrays.asList(target);
        LinkedList<Integer> path = new LinkedList<>();
        int cur = target;
        while (cur != -1) {
            path.addFirst(cur);
            cur = parent[cur];
        }
        return path;
    }

    // -----------------------------
    // inner class to store result
    // -----------------------------
    public static class Result {
        public final double[] dist;
        public final int[] parent;

        public Result(double[] dist, int[] parent) {
            this.dist = dist;
            this.parent = parent;
        }
    }
}
