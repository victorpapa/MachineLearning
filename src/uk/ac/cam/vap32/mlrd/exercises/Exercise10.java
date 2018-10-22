package uk.ac.cam.vap32.mlrd.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise10;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise10 implements IExercise10 {

    private void add(Map<Integer, Set<Integer>> result, int a, int b){

        Set<Integer> aux;

        try{
            aux = result.get(a);
            aux.add(b);
        }
        catch (NullPointerException e){
            aux = new HashSet<>();
            aux.add(b);
            result.put(a, aux);
        }
    }

    @Override
    public Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException {

        Map<Integer, Set<Integer>> result = new HashMap<>();

        BufferedReader in = new BufferedReader(new FileReader(new File(graphFile.toUri())));

        String line;
        char c;
        int x, y, count;

        while ((line = in.readLine()) != null){

            count = 0;
            c = line.charAt(0);

            x = y = 0;

            while (c <= '9' && c >= '0'){
                x = x * 10 + c - '0';
                c = line.charAt(++count);
            }

            c = line.charAt(++count);
            while (c <= '9' && c >= '0'){
                y = y * 10 + c - '0';
                try{
                    c = line.charAt(++count);
                }
                catch (StringIndexOutOfBoundsException e){
                    break;
                }
            }

            add(result, x, y);
            add(result, y, x);
        }

        return result;
    }

    @Override
    public Map<Integer, Integer> getConnectivities(Map<Integer, Set<Integer>> graph) {

        Map<Integer, Integer> result = new HashMap<>();
        Set<Integer> aux;

        for (int x : graph.keySet()){
            aux = graph.get(x);

            result.put(x, aux.size());
        }

        return result;
    }

    @Override
    public int getDiameter(Map<Integer, Set<Integer>> graph) {

        LinkedList<Integer> q = new LinkedList<>();
        Map<Integer, Integer> dist = new HashMap<>();
        Set<Integer> aux;

        for (int i : graph.keySet()){
            dist.put(i, Integer.MAX_VALUE);
        }

        int source = graph.entrySet().iterator().next().getKey();
        int node, d;
        int max = -1, nodeMax = source;

        dist.put(source, 0);
        q.add(source);

        while (!q.isEmpty()){
            node = q.removeFirst();
            d = dist.get(node);

            aux = graph.get(node);

            for (int neighbour : aux){
                if (d + 1 < dist.get(neighbour)){
                    dist.put(neighbour, d+1);
                    q.addLast(neighbour);

                    if (d + 1 > max){
                        max = d+1;
                        nodeMax = neighbour;
                    }
                }
            }
        }

        source = nodeMax;
        max = -1;

        for (int i : graph.keySet()){
            dist.put(i, Integer.MAX_VALUE);
        }

        dist.put(source, 0);
        q.add(source);

        while (!q.isEmpty()){
            node = q.removeFirst();
            d = dist.get(node);

            aux = graph.get(node);

            for (int neighbour : aux){
                if (d + 1 < dist.get(neighbour)){
                    dist.put(neighbour, d+1);
                    q.addLast(neighbour);

                    if (d + 1 > max){
                        max = d+1;
                    }
                }
            }
        }

        return max;
    }
}
