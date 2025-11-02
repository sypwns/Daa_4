package graph.scc;

import java.util.*;
import graph.common.Graph;
import graph.common.Metrics;

public class SCCTarjan {
    private final Graph g;
    private final Metrics m;

    private int time = 0;
    private int[] disc, low, compOf;
    private boolean[] onStack;
    private Deque<Integer> st;
    private List<List<Integer>> components;

    public SCCTarjan(Graph g, Metrics m) {
        this.g = g;
        this.m = m;
    }

    public List<List<Integer>> run() {
        int n = g.n;
        disc = new int[n]; Arrays.fill(disc, -1);
        low = new int[n];
        compOf = new int[n]; Arrays.fill(compOf, -1);
        onStack = new boolean[n];
        st = new ArrayDeque<>();
        components = new ArrayList<>();

        m.startTimer();
        for (int v = 0; v < n; v++) {
            if (disc[v] == -1) dfs(v);
        }
        m.stopTimer();
        return components;
    }

    private void dfs(int u) {
        disc[u] = low[u] = time++;
        m.dfsVisits++;
        st.push(u); onStack[u] = true;
        for (Graph.Edge e : g.adj.get(u)) {
            m.dfsEdges++;
            int v = e.to;
            if (disc[v] == -1) {
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }
        if (low[u] == disc[u]) {
            List<Integer> comp = new ArrayList<>();
            while (true) {
                int w = st.pop(); onStack[w] = false;
                compOf[w] = components.size();
                comp.add(w);
                if (w == u) break;
            }
            components.add(comp);
        }
    }

    public int[] getComponentOf() { return compOf; }
}
