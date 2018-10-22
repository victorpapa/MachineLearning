package uk.ac.cam.vap32.mlrd.exercises;

import javafx.util.Pair;
import uk.ac.cam.cl.mlrd.utils.BestFit;
import uk.ac.cam.cl.mlrd.utils.BestFit.Point;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer.tokenize;
import static uk.ac.cam.cl.mlrd.utils.ChartPlotter.plotLines;

public class Exercise3 {
    private static Path dataDirectory = Paths.get("C:\\Users\\victo\\Desktop\\DataML\\large_dataset");
    private static Map<String, Integer> frequencies = new HashMap<>();
    private static List<Point> dataPoints = new ArrayList<>();
    private static int types=0, tokens=0;

    private static void printFrequencies(){
        for (String str : frequencies.keySet()){
            System.out.println(str + " " + frequencies.get(str));
        }
    }

    private static void getFrequencies(){
        List<String> myList;

        try {
            DirectoryStream<Path> files = Files.newDirectoryStream(dataDirectory);

            for (Path item : files){
                myList = tokenize(item);

                for (String str : myList){
                    try{
                        int x = frequencies.get(str);
                        frequencies.put(str, x+1);
                    }
                    catch (NullPointerException e){
                        frequencies.put(str, 1);
                    }
                }
            }
        }
        catch (IOException e){
            System.out.println("Can't read the reviews!");
        }
    }

    private static void getTokens(){
        List<String> myList;
        int nextPower = 1;

        try {
            DirectoryStream<Path> files = Files.newDirectoryStream(dataDirectory);

            for (Path item : files){
                myList = tokenize(item);

                for (String str : myList){
                    try{
                        int x = frequencies.get(str);
                        frequencies.put(str, x+1);
                    }
                    catch (NullPointerException e){
                        frequencies.put(str, 1);
                        types ++;
                    }

                    tokens++;

                    if (nextPower == tokens){
                        dataPoints.add(new Point(Math.log(tokens), Math.log(types)));
                        nextPower <<= 1;
                    }
                }
            }

            dataPoints.add(new Point(Math.log(tokens), Math.log(types)));
        }
        catch (IOException e){
            System.out.println("Can't read the reviews!");
        }
    }

    private static double expectedFrequency(BestFit.Line l, int rank){

        return Math.exp(Math.log(rank) * l.gradient + l.yIntercept);
    }

    private static void plotFrequencies() {
        List<Point> myList = new ArrayList<>();
        List<Point> myLogList = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Map<Point, Double> lineFitter = new HashMap<>();
        List<Pair<Integer, String>> v = new ArrayList<>();

        for (String str : frequencies.keySet()){
            v.add(new Pair(frequencies.get(str), str));
        }

        Collections.sort(v, new Comparator<Pair<Integer, String>>() {
            @Override
            public int compare(Pair<Integer, String> o1, Pair<Integer, String> o2) {
                if (o1.getKey() < o2.getKey())
                    return 1;

                if (o1.getKey() == o2.getKey())
                    return 0;

                return -1;
            }
        });

        for (int i=0; i<10000; i++){
            int val = v.get(i).getKey();

            myList.add(new Point(i, val));

            Point p = new Point(Math.log(i+1), Math.log(val));
            myLogList.add(p);
            lineFitter.put(p, (double)(val));
        }

        List<String> words = new ArrayList<>();
        List<Point> tenWords = new ArrayList<>();
        words.add("well");
        words.add("satisfying");
        words.add("great");
        words.add("awesome");
        words.add("lacking");
        words.add("uncomfortable");
        words.add("annoying");
        words.add("like");
        words.add("nice");
        words.add("interesting");

        for (String str : words){
            int val = frequencies.get(str);

            //TODO: Optimize this with a binary search
            for (int i=0; i<v.size(); i++){
                if (v.get(i).getValue().equals(str)){
                    tenWords.add(new Point(i, val));
                    indices.add(i);
                    System.out.println(str + " " + val);
                }
            }
        }

        System.out.println();
        plotLines(myList, tenWords);
        BestFit.Line l = BestFit.leastSquares(lineFitter);
        List<Point> fitter = new ArrayList<>();

        for (int i=0; i<10000; i++){
            fitter.add(new Point(Math.log(i+1), Math.log(i+1) * l.gradient + l.yIntercept));
        }

        //plotLines(myLogList, fitter);

        for (int i : indices){
            System.out.println("Using function: " + Math.log(expectedFrequency(l, i+1)));
            System.out.println("Actual frequency: " + Math.log(v.get(i).getKey()));

            System.out.println();
        }

        System.out.println(l.gradient + " " + l.yIntercept);

        frequencies.clear();
        for (int i=0; i<5; i++)
            System.out.println(v.get(i).getKey());

        getTokens();System.out.print(tokens);
        //plotLines(dataPoints);
    }

    public static void main(String[] args) {
        getFrequencies();
        plotFrequencies();
    }
}
