package uk.ac.cam.vap32.mlrd.exercises;


import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer.tokenize;

public class Exercise1 implements IExercise1 {

    @Override
    public Map<Path, Sentiment> simpleClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {

        Map<Path, Sentiment> result = new HashMap<>();

        Map<String, Integer> sentiments = new HashMap<>();

        List<String> myList;

        String word = "", polarity;

        myList = tokenize(lexiconFile);

        for (int i=0; i<myList.size(); i++){

            /*System.out.print(myList.get(i));
            if (i % 9 == 2 || i % 9 == 5) System.out.print(" ");
            if (i % 9 == 8) System.out.println();*/

            if (i % 9 == 2)
                word = myList.get(i);
            else
            if (i % 9 == 8){
                polarity = myList.get(i);
                sentiments.put(word, (polarity.equals("positive") ? 1 : -1));

                //System.out.println(word + " " + polarity);
            }
        }

        int sum;

        for (Path myPath : testSet){

            sum = 0;

            myList = tokenize(myPath);

            for (String str : myList) {
                if (sentiments.get(str) != null) {
                    sum += sentiments.get(str);
                }
            }

            if (sum >= 0)
                result.put(myPath, Sentiment.POSITIVE);
            else
                result.put(myPath, Sentiment.NEGATIVE);
        }

        return result;
    }

    @Override
    public double calculateAccuracy(Map<Path, Sentiment> trueSentiments, Map<Path, Sentiment> predictedSentiments) {

        double c = 0, i = 0;

        for (Map.Entry myEntry : trueSentiments.entrySet()){
            Path myPath = (Path)myEntry.getKey();
            Sentiment mySentiment = predictedSentiments.get(myPath);

            if (myEntry.getValue().equals(mySentiment))
                c ++;
            else
                i ++;
        }

        double result = c / (c + i);
        return result;
    }

    @Override
    public Map<Path, Sentiment> improvedClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        Map<Path, Sentiment> result = new HashMap<>();

        Map<String, Integer> sentiments = new HashMap<>();

        List<String> myList;

        String word = "", polarity, intensity = "";

        myList = tokenize(lexiconFile);

        int value, scale;

        for (int i=0; i<myList.size(); i++){
            if (i % 9 == 2)
                word = myList.get(i);
            else
            if (i % 9 == 5) {
                intensity = myList.get(i);
            }
            else
            if (i % 9 == 8){
                polarity = myList.get(i);

                if (intensity.equals("strong"))
                    scale = 2;
                else
                    scale = 1;

                if (polarity.equals("positive"))
                    value = 1;
                else
                    value = -1;

                sentiments.put(word, value * scale);

                //System.out.println(word + " " + polarity);
            }
        }

        int sum;

        for (Path myPath : testSet){

            sum = 0;

            myList = tokenize(myPath);

            for (String str : myList) {
                if (sentiments.get(str) != null) {
                    sum += sentiments.get(str);
                }
            }

            if (sum > 0)
                result.put(myPath, Sentiment.POSITIVE);
            else
                result.put(myPath, Sentiment.NEGATIVE);
        }

        return result;
    }
}
