package uk.ac.cam.vap32.mlrd.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.DataPreparation1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.utils.DataSplit;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Set;

import static uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer.tokenize;

public class Exercise4 implements IExercise4{
    private static Path dataDirectory = Paths.get("C:\\Users\\victo\\Desktop\\DataML\\sentiment_dataset");

    private static BigDecimal coeff(int n, int k){
        BigDecimal result = BigDecimal.ONE;

        for (int i=n-k+1; i<=n; i++) {
            BigDecimal I = new BigDecimal(Integer.toString(i));
            result = result.multiply(I);
        }

        for (int i=1; i<=k; i++){
            BigDecimal I = new BigDecimal(Integer.toString(i));
            result = result.divide(I, BigDecimal.ROUND_HALF_UP);
        }

        return result;
    }

    @Override
    public Map<Path, Sentiment> magnitudeClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {

        Map<Path, Sentiment> result = new HashMap<>();
        Map<String, Integer> sentiments = new HashMap<>();
        List<String> myList;
        String word = "", polarity, intensity = "";
        int value, scale;
        int sum;

        myList = tokenize(lexiconFile);

        for (int i=0; i<myList.size(); i++){
            if (i % 9 == 2)
                word = myList.get(i);
            else
            if (i % 9 == 5)
                intensity = myList.get(i);
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
            }
        }

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
    public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA, Map<Path, Sentiment> classificationB) {

        int positive, negative, nulll;
        int sys1, sys2;

        positive = negative = nulll = 0;
        int count = 0;

        for (Map.Entry<Path, Sentiment> myEntry : actualSentiments.entrySet()){

            count++;

            if (count == 30) break;

            Path key = myEntry.getKey();
            Sentiment val = myEntry.getValue(); // the ground truth

            if (classificationA.get(key) == val)
                sys1 = 1;
            else
                sys1 = 0;

            if (classificationB.get(key) == val)
                sys2 = 1;
            else
                sys2 = 0;

            if (sys1 > sys2)
                positive++;
            else if (sys1 < sys2)
                negative++;
            else
                nulll++;
        }

        //System.out.println(positive + " " + negative + " " + nulll);

        int n = 2 * ((nulll + 1) / 2) + positive + negative;
        int k = ((nulll + 1) / 2) + Math.min(positive, negative);

        //System.out.println(n + " " + k);

        BigDecimal p = BigDecimal.ZERO;

        for (int i=0; i<=k; i++) {
            p = p.add(coeff(n, i));
        }

        p = p.divide((BigDecimal.ONE.add(BigDecimal.ONE)).pow(n-1), 20, BigDecimal.ROUND_HALF_UP);

        return p.doubleValue();
    }
}
