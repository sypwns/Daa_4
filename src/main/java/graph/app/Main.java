package graph.app;

import graph.common.Graph;
import graph.common.Metrics;
import graph.scc.SCCTarjan;
import graph.topo.Topological;
import graph.dagsp.DAGShortestLongest;

import java.io.FileReader;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class Main {
    public static void main(String[] args) throws Exception {
        // === 1. Load graph from JSON ===
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(new FileReader("data/tasks.json"));

        boolean directed = (boolean) obj.get("directed");
        long nLong = (long) obj.get("n");
        int n = (int) nLong;

        JSONArray edgesArr = (JSONArray) obj.get("edges");
        Graph dag = new Graph(n, directed);

        for (Object o : edgesArr) {
            JSONObject e = (JSONObject) o;
            int u = ((Long) e.get("u")).intValue();
            int v = ((Long) e.get("v")).intValue();
            double w = ((Number) e.get("w")).doubleValue();
            dag.addEdge(u, v, w);
        }

        int source = -1;
        if (obj.containsKey("source")) {
            source = ((Long) obj.get("source")).intValue();
        }

        Metrics m = new Metrics();

        // === 1.1. SCC Detection ===
        System.out.println("=== Strongly Connected Components ===");
        SCCTarjan scc = new SCCTarjan(dag, m);
        List<List<Integer>> comps = scc.run();
        int[] compOf = scc.getComponentOf();

        for (int i = 0; i < comps.size(); i++) {
            System.out.println("Component " + i + ": " + comps.get(i));
        }

        // === Build condensation graph (DAG of components) ===
        Graph cond = Graph.buildCondensation(dag, compOf, comps.size());
        System.out.println("Condensation DAG built with " + comps.size() + " components.");

        // Print adjacency of condensation DAG
        System.out.println("\nCondensation DAG adjacency:");
        for (int u = 0; u < cond.n; u++) {
            System.out.print(u + ": ");
            for (Graph.Edge e : cond.adj.get(u)) {
                System.out.print(e.to + " ");
            }
            System.out.println();
        }

        // === 1.2. Topological Sort ===
        System.out.println("\n=== Topological Sort of Condensation DAG ===");
        Topological topo = new Topological(cond, new Metrics());
        Optional<List<Integer>> orderOpt = topo.kahnOrder();
        if (!orderOpt.isPresent()) {
            System.out.println("Condensation is not a DAG? Unexpected.");
            return;
        }

        List<Integer> compOrder = orderOpt.get();
        System.out.println("Topological order of components: " + compOrder);

        // Derived order of original tasks after SCC compression
        List<Integer> expandedOrder = new ArrayList<>();
        for (int cid : compOrder) {
            expandedOrder.addAll(comps.get(cid));
        }
        System.out.println("Derived order of original tasks after SCC compression: " + expandedOrder);

        // === 1.3. Shortest and Longest Paths in DAG ===
        System.out.println("\n=== Shortest and Longest Paths on DAG ===");
        DAGShortestLongest solver = new DAGShortestLongest(cond, new Metrics());
        List<Integer> topoCompOrder = compOrder;

        if (source >= 0) {
            int compSource = compOf[source];
            DAGShortestLongest.Result res = solver.shortestFrom(compSource, topoCompOrder);
            System.out.println("Shortest distances (component-level) from comp " + compSource + ":");
            for (int i = 0; i < res.dist.length; i++) {
                System.out.println(i + ": " + res.dist[i]);
            }

            // Reconstruct one path
            for (int t = 0; t < res.dist.length; t++) {
                if (res.dist[t] < Double.POSITIVE_INFINITY) {
                    List<Integer> path = DAGShortestLongest.reconstruct(res.parent, t);
                    System.out.println("Path to " + t + ": " + path + " dist=" + res.dist[t]);
                }
            }
        }

        // Longest path (critical path)
        DAGShortestLongest.Result longest = solver.longestPath(topoCompOrder);
        double best = Double.NEGATIVE_INFINITY;
        int bestIdx = -1;
        for (int i = 0; i < longest.dist.length; i++) {
            if (longest.dist[i] > best) {
                best = longest.dist[i];
                bestIdx = i;
            }
        }

        System.out.println("Critical (longest) path length = " + best + ", ending at comp " + bestIdx);
        if (bestIdx != -1) {
            List<Integer> compPath = DAGShortestLongest.reconstruct(longest.parent, bestIdx);
            System.out.println("Component path: " + compPath);

            // Expand to original nodes
            List<Integer> expandedPath = new ArrayList<>();
            for (int cid : compPath) expandedPath.addAll(comps.get(cid));
            System.out.println("Expanded critical path (original tasks): " + expandedPath);
        }

        System.out.println("\nDone.");
    }
}
