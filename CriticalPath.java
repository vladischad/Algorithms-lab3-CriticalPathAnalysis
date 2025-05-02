/**
 * CriticalPath.java
 *
 * This program performs critical path analysis on a project represented by a directed acyclic graph (DAG).
 * It reads an activity-node graph from a file (in adjacency matrix format), converts it into an adjacency list,
 * and calculates the earliest completion time (EC), latest completion time (LC), and slack for each activity.
 * 
 * Usage: java CriticalPath <filename>
 * The input file must be formatted with whitespace-separated values, including a header row and column.
 *
 * Author:  Vladyslav (Vlad) Maliutin
 *          CS 421 Spring 2025
 */
import java.io.*;
import java.util.*;

public class CriticalPath {

    /**
     * Represents a directed edge to another node.
     */
    static class Edge {
        int to;
                /**
         * Constructs an edge pointing to the specified destination node.
         * @param to the index of the destination node
         */
        Edge(int to) {
            this.to = to;
        }
    }

    static List<List<Edge>> adjList = new ArrayList<>();
    static int[] EC, LC, procTime;
    static int n;
    static String[] nodeNames;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java CriticalPath <filename>");
            return;
        }

        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        nodeNames = br.readLine().trim().split("\\s+");
        n = nodeNames.length;

        int[][] matrix = new int[n][n];
        procTime = new int[n];
        String line;
        for (int i = 0; i < n; i++) {
            line = br.readLine();
            String[] parts = line.trim().split("\\s+");
            for (int j = 0; j < n; j++) {
                matrix[i][j] = Integer.parseInt(parts[j + 1]);
                if (matrix[i][j] != -1) procTime[j] = matrix[i][j];
            }
        }

        buildGraph(matrix);
        EC = new int[n];
        LC = new int[n];
        Arrays.fill(LC, Integer.MAX_VALUE);

        List<Integer> topoOrder = topologicalSort();
        computeEC(topoOrder);
        computeLC(topoOrder);

        printResults();
    }

    /**
     * Builds the adjacency list representation of the graph from an adjacency matrix.
     * @param matrix the adjacency matrix representing edges between nodes
     */
    static void buildGraph(int[][] matrix) {
        for (int i = 0; i < n; i++) {
            adjList.add(new ArrayList<>());
        }
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (matrix[u][v] != -1) {
                    adjList.get(u).add(new Edge(v));
                }
            }
        }
    }

    /**
     * Performs a topological sort on the directed acyclic graph (DAG).
     * @return a list of node indices in topological order
     */
    static List<Integer> topologicalSort() {
        int[] inDegree = new int[n];
        for (int u = 0; u < n; u++) {
            for (Edge e : adjList.get(u)) {
                inDegree[e.to]++;
            }
        }
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) queue.add(i);
        }
        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);
            for (Edge e : adjList.get(u)) {
                inDegree[e.to]--;
                if (inDegree[e.to] == 0) queue.add(e.to);
            }
        }
        return order;
    }

    /**
     * Computes the earliest completion times (EC) for all nodes based on topological order.
     * @param order list of nodes in topological order
     */
    static void computeEC(List<Integer> order) {
        for (int u : order) {
            for (Edge e : adjList.get(u)) {
                EC[e.to] = Math.max(EC[e.to], EC[u] + procTime[e.to]);
            }
        }
    }

    /**
     * Computes the latest completion times (LC) for all nodes based on reverse topological order.
     * @param order list of nodes in topological order
     */
    static void computeLC(List<Integer> order) {
        LC[order.get(order.size() - 1)] = EC[order.get(order.size() - 1)];
        for (int i = order.size() - 1; i >= 0; i--) {
            int u = order.get(i);
            for (Edge e : adjList.get(u)) {
                LC[u] = Math.min(LC[u], LC[e.to] - procTime[e.to]);
            }
        }
    }

    /**
     * Prints the earliest and latest completion times along with slack for each node.
     */
    static void printResults() {
        System.out.println("Activity Node\tEC\tLC\tSlackTime");
        System.out.println("-----------------------------------------------------");
        for (int u = 0; u < n; u++) {
            int slack = LC[u] - EC[u];
            System.out.printf("%s\t\t%d\t%d\t%d\n", nodeNames[u], EC[u], LC[u], slack);
        }
    }
}
