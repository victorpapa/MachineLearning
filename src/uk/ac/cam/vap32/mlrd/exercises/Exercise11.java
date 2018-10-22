package uk.ac.cam.vap32.mlrd.exercises;

import sun.awt.image.ImageWatched;
import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise11;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise11 implements IExercise11 {
    @Override
    public Map<Integer, Double> getNodeBetweenness(Path graphFile) throws IOException {

        Exercise10 helper = new Exercise10();
        Map<Integer, Set<Integer>> graph = helper.loadGraph(graphFile);

        Map<Integer, Double> result = new HashMap<>();

        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> nPaths = new HashMap<>();
        Map<Integer, Set<Integer>> pred = new HashMap<>();
        Map<Integer, Double> values = new HashMap<>();
        LinkedList<Integer> q = new LinkedList<>();
        LinkedList<Integer> s = new LinkedList<>();

        int node, d;

        for (int x : graph.keySet()){
            result.put(x, 0.0);
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

                    values.put(v, nr + ((double)nPaths.get(v) / (double)nPaths.get(node)) * (1.0 + values.get(node)));
                }

                if (node != source){
                    result.put(node, result.get(node) + values.get(node));
                }
            }
        }

        for (int key : result.keySet()){
            result.put(key, result.get(key) / 2.0);
        }

        return result;
    }
}
