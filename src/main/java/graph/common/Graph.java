package graph.common;

import java.util.*;

public class Graph {
    public int n;
    public List<List<Edge>> adj;
    public boolean directed;

    public static class Edge {
        public int to;
        public double w;

        public Edge(int to, double w) {
            this.to = to;
            this.w = w;
        }
    }

    public Graph(int n) {
        this.n = n;
        adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }

    // ✅ Новый конструктор
    public Graph(int n, boolean directed) {
        this(n);
        this.directed = directed;
    }

    public void addEdge(int u, int v, double w) {
        adj.get(u).add(new Edge(v, w));
        if (!directed) adj.get(v).add(new Edge(u, w));
    }

    public static Graph buildCondensation(Graph g, int[] compOf, int compCount) {
        Graph dag = new Graph(compCount, true);
        Set<String> added = new HashSet<>();

        for (int u = 0; u < g.n; u++) {
            for (Edge e : g.adj.get(u)) {
                int a = compOf[u], b = compOf[e.to];
                if (a != b) {
                    String key = a + "-" + b;
                    if (!added.contains(key)) {
                        dag.addEdge(a, b, 1.0);
                        added.add(key);
                    }
                }
            }
        }

        return dag;
    }
}
