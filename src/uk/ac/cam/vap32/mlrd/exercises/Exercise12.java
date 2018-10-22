package uk.ac.cam.vap32.mlrd.exercises;

import javafx.util.Pair;
import org.omg.CORBA.INTERNAL;
import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise12;

import java.util.*;

public class Exercise12 implements IExercise12 {
    @Override
    public List<Set<Integer>> GirvanNewman(Map<Integer, Set<Integer>> graph, int minimumComponents) {

        int nComponents = getComponents(graph).size();
        int nEdges = getNumberOfEdges(graph);
        double max;

        List<Set<Integer>> result = new ArrayList<>();
        Map<Integer, Map<Integer, Double>> betweennesses = getEdgeBetweenness(graph);
        List<Pair<Integer, Integer>> bestNodes = new ArrayList<>();

        while (nComponents < minimumComponents && nEdges > 0){
            max = -1;

            for (int i : betweennesses.keySet()){
                for (int j : betweennesses.get(i).keySet()) {

                    double x = betweennesses.get(i).get(j);

                    if (x > max) {
                        max = x;
                        bestNodes.clear();

                        bestNodes.add(new Pair<>(i, j));
                    } else if (x == max) {
                        bestNodes.add(new Pair<>(i, j));
                    }
                }
            }

            for (Pair<Integer, Integer> p : bestNodes){
                graph.get(p.getKey()).remove(p.getValue());
            }

            result = getComponents(graph);
            nComponents = result.size();
            nEdges = getNumberOfEdges(graph);
            betweennesses = getEdgeBetweenness(graph);
        }

        for (Set<Integer> s : result){
            System.out.println(s.size());
        }

        return result;
    }

    @Override
    public int getNumberOfEdges(Map<Integer, Set<Integer>> graph) {
        int sum = 0;

        for (int i : graph.keySet()){
            sum += graph.get(i).size();
        }

        return sum / 2;
    }

    private void dfs(int node, Map<Integer, Set<Integer>> graph, boolean[] visited, Set<Integer> aux){
        visited[node] = true;
        Set<Integer> neighbours = graph.get(node);
        aux.add(node);

        for (int next : neighbours){
            if (!visited[next]){
                dfs(next, graph, visited, aux);
            }
        }
    }

    @Override
    public List<Set<Integer>> getComponents(Map<Integer, Set<Integer>> graph) {

        int max = -1;

        for (int i : graph.keySet()){

            if (max < i)
                max = i;

            for (int j : graph.get(i)){
                if (max < j)
                    max = j;
            }
        }

        boolean visited[] = new boolean[max+1];
        List<Set<Integer>> result = new ArrayList<>();
        Set<Integer> aux;

        for (int i : graph.keySet()){
            if (!visited[i]){
                aux = new HashSet<>();
                dfs(i, graph, visited, aux);
                result.add(aux);
            }
        }

        return result;
    }

    @Override
    public Map<Integer, Map<Integer, Double>> getEdgeBetweenness(Map<Integer, Set<Integer>> graph) {
        Exercise10 helper = new Exercise10();

        Map<Integer, Map<Integer, Double>> result = new HashMap<>();

        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> nPaths = new HashMap<>();
        Map<Integer, Set<Integer>> pred = new HashMap<>();
        Map<Integer, Double> values = new HashMap<>();
        LinkedList<Integer> q = new LinkedList<>();
        LinkedList<Integer> s;

        Map<Integer, Double> aux;

        int node, d;

        for (int x : graph.keySet()){
            for (int y : graph.get(x)) {
                aux = new HashMap<>();
                aux.put(y, 0.0);
                result.put(x, aux);
            }
        }

        for (int source : graph.keySet()){

            for (int x : graph.keySet()) {
                dist.put(x, Integer.MAX_VALUE);
                nPaths.put(x, 0);
                pred.put(x, new HashSet<>());
                values.put(x, 0.0);
            }

            s = new LinkedList<>();

            dist.put(source, 0);
            nPaths.put(source, 1);
            q.addLast(source);

            while (!q.isEmpty()){
                node = q.removeFirst();
                s.addLast(node);
                d = dist.get(node);

                for (int neighbour : graph.get(node)){
                    if (dist.get(neighbour) == Integer.MAX_VALUE){
                        dist.put(neighbour, d+1);
                        q.addLast(neighbour);
                    }

                    if (dist.get(neighbour) == d + 1){
                        nPaths.put(neighbour, nPaths.get(neighbour) + nPaths.get(node));
                        pred.get(neighbour).add(node);
                    }
                }
            }

            while (!s.isEmpty()){
                node = s.removeLast();

                for (int v : pred.get(node)){
                    double nr = values.get(v);
                    double c = ((double)nPaths.get(v) / (double)nPaths.get(node)) * (1.0 + values.get(node));

                    aux = result.get(v);
                    aux.put(node, aux.getOrDefault(node, 0.0) + c);

                    values.put(v, nr + c);
                }
            }
        }

        return result;
    }
}
