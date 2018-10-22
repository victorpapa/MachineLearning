package uk.ac.cam.vap32.mlrd.exercises;

import javafx.util.Pair;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise5;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer.tokenize;

public class Exercise5 implements IExercise5 {

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

    public Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) throws IOException {

        Map<Sentiment, Double> result = new HashMap<>();

        double pos, neg;
        pos = neg = 0;

        for (Map.Entry<Path, Sentiment> myEntry : trainingSet.entrySet()){
            if (myEntry.getValue() == Sentiment.POSITIVE)
                pos ++;
            else
                neg++;
        }

        result.put(Sentiment.POSITIVE, pos / (pos + neg));
        result.put(Sentiment.NEGATIVE, neg / (pos + neg));

        return result;
    }

    public Map<Path, Sentiment> naiveBayes(Set<Path> testSet, Map<String, Map<Sentiment, Double>> tokenLogProbs, Map<Sentiment, Double> classProbabilities) throws IOException {

        Map<Path, Sentiment> result = new HashMap<>();
        List<String> myList;

        for (Path myPath : testSet){
            myList = tokenize(myPath);

            double c1 = Math.log(classProbabilities.get(Sentiment.POSITIVE));
            double c2 = Math.log(classProbabilities.get(Sentiment.NEGATIVE));

            for (String str : myList){
                if (tokenLogProbs.get(str) != null) {
                    c1 += tokenLogProbs.get(str).get(Sentiment.POSITIVE);
                    c2 += tokenLogProbs.get(str).get(Sentiment.NEGATIVE);
                }
            }

            if (c1 >= c2){
                result.put(myPath, Sentiment.POSITIVE);
            }
            else{
                result.put(myPath, Sentiment.NEGATIVE);
            }

        }

        return result;
    }

    public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
        Map<String, Map<Sentiment, Double>> result = new HashMap<>();
        List<String> myList;

        double sumP, sumN;

        sumP = sumN = 0;

        for (Map.Entry<Path, Sentiment> myEntry : trainingSet.entrySet()) {
            myList = tokenize(myEntry.getKey());

            for (String str : myList){

                Map<Sentiment, Double> aux = result.get(str);

                if (aux == null){

                    aux = new HashMap<>();
                    double p, n;


                    if (myEntry.getValue() == Sentiment.NEGATIVE){
                        p = 1.0; n = 2.0;
                        sumN++;
                    }
                    else {
                        p = 2.0; n = 1.0;
                        sumP++;
                    }

                    aux.put(Sentiment.POSITIVE, p);
                    aux.put(Sentiment.NEGATIVE, n);

                    result.put(str, aux);
                }
                else{

                    double x = aux.get(myEntry.getValue());
                    aux.put(myEntry.getValue(), x+1);

                    if (myEntry.getValue() == Sentiment.NEGATIVE)
                        sumN++;
                    else
                        sumP++;
                }
            }
        }

        for (Map.Entry<String, Map<Sentiment, Double>> myEntry : result.entrySet()){
            Map<Sentiment, Double> val = myEntry.getValue();

            double pos = val.get(Sentiment.POSITIVE);
            double neg = val.get(Sentiment.NEGATIVE);

            double x = pos / (sumP + result.size());
            double y = neg / (sumN + result.size());

            val.put(Sentiment.POSITIVE, (Math.log(x)));
            val.put(Sentiment.NEGATIVE, (Math.log(y)));
        }

        return result;
    }

    @Override
    public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed) {

        List<Map<Path, Sentiment>> result = new ArrayList<>();
        List<Pair<Path, Sentiment>> aux = new ArrayList<>();
        Map<Path, Sentiment> myMap;
        int size = dataSet.size() / 10;

        for (Map.Entry myEntry : dataSet.entrySet()){
            aux.add(new Pair(myEntry.getKey(), myEntry.getValue()));
        }

        Collections.shuffle(aux, new Random((seed)));

        for (int i=0; i<10; i++){

            myMap = new HashMap<>();

            for (int j = i * size; j < (i+1) * size; j++){
                myMap.put(aux.get(j).getKey(), aux.get(j).getValue());
            }

            result.add(myMap);
        }

        return result;
    }

    @Override
    public List<Map<Path, Sentiment>> splitCVStratifiedRandom(Map<Path, Sentiment> dataSet, int seed) {

        List<Pair<Path, Sentiment>> positives = new ArrayList<>();
        List<Pair<Path, Sentiment>> negatives = new ArrayList<>();
        List<Map<Path, Sentiment>> result = new ArrayList<>();
        Map<Path, Sentiment> myMap;
        int size = (dataSet.size() / 10) / 2;

        for (Map.Entry myEntry : dataSet.entrySet()){
            if (myEntry.getValue() == Sentiment.POSITIVE){
                positives.add(new Pair(myEntry.getKey(), myEntry.getValue()));
            }
            else{
                negatives.add(new Pair(myEntry.getKey(), myEntry.getValue()));
            }
        }

        Collections.shuffle(positives, new Random((seed)));
        Collections.shuffle(negatives, new Random((seed)));

        for (int i=0; i<10; i++){
            myMap = new HashMap<>();

            for (int j = i * size; j < (i+1) * size; j++){
                myMap.put(positives.get(j).getKey(), positives.get(j).getValue());
                myMap.put(negatives.get(j).getKey(), negatives.get(j).getValue());
            }

            result.add(myMap);
        }

        return result;
    }

    @Override
    public double[] crossValidate(List<Map<Path, Sentiment>> folds) throws IOException {

        Map<Path, Sentiment> myMap = new HashMap<>();
        double[] result = new double[10];
        double p;

        for (int i=0; i < 10; i++){

            myMap.clear();

            for (int j=0; j < 10; j++){
                if (j != i){
                    myMap.putAll(folds.get(j));
                }
            }

            Map<String, Map<Sentiment, Double>> aux1 = calculateSmoothedLogProbs(myMap);
            Map<Sentiment, Double> aux2 = calculateClassProbabilities(myMap);
            Map<Path, Sentiment> aux = naiveBayes(folds.get(i).keySet(), aux1, aux2);

            p = calculateAccuracy(aux, folds.get(i));
            result[i] = p;
        }

        for (int i=0; i<10; i++)
            System.out.print(result[i] + " ");

        System.out.println();

        return result;
    }

    @Override
    public double cvAccuracy(double[] scores) {

        double sum = 0;

        for (int i=0; i<10; i++)
            sum += scores[i];

        return sum / 10;
    }

    @Override
    public double cvVariance(double[] scores) {

        double sum = 0;
        double avg = cvAccuracy(scores);

        for (int i=0; i<10; i++)
            sum += (avg - scores[i]) * (avg - scores[i]);

        return sum/10;
    }
}
