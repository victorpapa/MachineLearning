package uk.ac.cam.vap32.mlrd.exercises;

import javafx.util.Pair;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer.tokenize;

public class Exercise6 implements IExercise6{

    public Exercise6(){}

    private Exercise6(Map<Path, NuancedSentiment> dataSet) throws IOException {

        Map<String, Map<NuancedSentiment, Double>> logProbs = calculateNuancedLogProbs(dataSet);
        Map<NuancedSentiment, Double> classProbabilities = calculateClassProbabilities(dataSet);

        System.out.println(classProbabilities);
        Map<Path, NuancedSentiment> probs = nuancedClassifier(dataSet.keySet(), logProbs, classProbabilities);

        System.out.println(probs);

        System.out.println(nuancedAccuracy(dataSet, probs));

        List<Map<Path, NuancedSentiment>> aux = splitCVRandom(dataSet, 0);
        double[] result = crossValidate(aux);
    }

    public List<Map<Path, NuancedSentiment>> splitCVRandom(Map<Path, NuancedSentiment> dataSet, int seed) {

        List<Map<Path, NuancedSentiment>> result = new ArrayList<>();
        List<Pair<Path, NuancedSentiment>> aux = new ArrayList<>();
        Map<Path, NuancedSentiment> myMap;
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

    public double[] crossValidate(List<Map<Path, NuancedSentiment>> folds) throws IOException {

        Map<Path, NuancedSentiment> myMap = new HashMap<>();
        double[] result = new double[10];
        double p;

        for (int i=0; i < 10; i++){

            myMap.clear();

            for (int j=0; j < 10; j++){
                if (j != i){
                    myMap.putAll(folds.get(j));
                }
            }

            Map<String, Map<NuancedSentiment, Double>> aux1 = calculateNuancedLogProbs(myMap);
            Map<NuancedSentiment, Double> aux2 = calculateClassProbabilities(myMap);
            Map<Path, NuancedSentiment> aux = nuancedClassifier(folds.get(i).keySet(), aux1, aux2);

            p = nuancedAccuracy(aux, folds.get(i));
            result[i] = p;
        }

        for (int i=0; i<10; i++)
            System.out.print(result[i] + " ");

        System.out.println();

        return result;
    }

    public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) throws IOException {

        Map<NuancedSentiment, Double> result = new HashMap<>();

        double pos, neg, neutral;
        pos = neg = neutral = 0;

        for (Map.Entry<Path, NuancedSentiment> myEntry : trainingSet.entrySet()){
            if (myEntry.getValue() == NuancedSentiment.POSITIVE)
                pos ++;
            else
            if (myEntry.getValue() == NuancedSentiment.NEGATIVE)
                neg++;
            else
                neutral++;
        }

        result.put(NuancedSentiment.POSITIVE, pos / (pos + neg + neutral));
        result.put(NuancedSentiment.NEGATIVE, neg / (pos + neg + neutral));
        result.put(NuancedSentiment.NEUTRAL, neutral / (pos + neg + neutral));

        return result;
    }

    @Override
    public Map<String, Map<NuancedSentiment, Double>> calculateNuancedLogProbs(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        Map<String, Map<NuancedSentiment, Double>> result = new HashMap<>();
        List<String> myList;

        double sumP, sumN, sumNeutral;

        sumP = sumN = sumNeutral = 0;

        for (Map.Entry<Path, NuancedSentiment> myEntry : trainingSet.entrySet()) {
            myList = tokenize(myEntry.getKey());

            for (String str : myList){

                Map<NuancedSentiment, Double> aux = result.get(str);

                if (aux == null){

                    aux = new HashMap<>();
                    double p, n, neutral;


                    if (myEntry.getValue() == NuancedSentiment.NEGATIVE){
                        neutral = p = 1.0; n = 2.0;
                        sumN++;
                    }
                    else
                    if (myEntry.getValue() == NuancedSentiment.POSITIVE){
                        p = 2.0; neutral = n = 1.0;
                        sumP++;
                    }
                    else {
                        neutral = 2.0; p = n = 1.0;
                        sumNeutral++;
                    }

                    aux.put(NuancedSentiment.POSITIVE, p);
                    aux.put(NuancedSentiment.NEGATIVE, n);
                    aux.put(NuancedSentiment.NEUTRAL, neutral);

                    result.put(str, aux);
                }
                else{

                    double x = aux.get(myEntry.getValue());
                    aux.put(myEntry.getValue(), x+1);

                    if (myEntry.getValue() == NuancedSentiment.POSITIVE)
                        sumP++;
                    else
                    if (myEntry.getValue() == NuancedSentiment.NEGATIVE)
                        sumN++;
                    else
                        sumNeutral++;
                }
            }
        }

        for (Map.Entry<String, Map<NuancedSentiment, Double>> myEntry : result.entrySet()){
            Map<NuancedSentiment, Double> val = myEntry.getValue();

            double pos = val.get(NuancedSentiment.POSITIVE);
            double neg = val.get(NuancedSentiment.NEGATIVE);
            double neutral = val.get(NuancedSentiment.NEUTRAL);

            double x = pos / (sumP + result.size());
            double y = neg / (sumN + result.size());
            double z = neutral / (sumNeutral + result.size());

            val.put(NuancedSentiment.POSITIVE, (Math.log(x)));
            val.put(NuancedSentiment.NEGATIVE, (Math.log(y)));
            val.put(NuancedSentiment.NEUTRAL, (Math.log(z)));
        }

        return result;
    }

    @Override
    public Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet, Map<String, Map<NuancedSentiment, Double>> tokenLogProbs, Map<NuancedSentiment, Double> classProbabilities) throws IOException {
        Map<Path, NuancedSentiment> result = new HashMap<>();
        List<String> myList;

        for (Path myPath : testSet){
            myList = tokenize(myPath);

            double c1 = Math.log(classProbabilities.get(NuancedSentiment.POSITIVE));
            double c2 = Math.log(classProbabilities.get(NuancedSentiment.NEGATIVE));
            double c3 = Math.log(classProbabilities.get(NuancedSentiment.NEUTRAL));

            for (String str : myList){
                if (tokenLogProbs.get(str) != null) {
                    c1 += tokenLogProbs.get(str).get(NuancedSentiment.POSITIVE);
                    c2 += tokenLogProbs.get(str).get(NuancedSentiment.NEGATIVE);
                    c3 += tokenLogProbs.get(str).get(NuancedSentiment.NEUTRAL);
                }
            }

            if (c1 > c2 && c1 > c3){
                result.put(myPath, NuancedSentiment.POSITIVE);
            }
            else
            if (c1 < c2 && c2 > c3){
                result.put(myPath, NuancedSentiment.NEGATIVE);
            }
            else{
                result.put(myPath, NuancedSentiment.NEUTRAL);
            }

        }

        return result;
    }

    @Override
    public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments, Map<Path, NuancedSentiment> predictedSentiments) {
        double c = 0, i = 0;

        for (Map.Entry myEntry : trueSentiments.entrySet()){
            Path myPath = (Path)myEntry.getKey();
            NuancedSentiment mySentiment = predictedSentiments.get(myPath);

            if (myEntry.getValue().equals(mySentiment))
                c ++;
            else
                i ++;
        }

        double result = c / (c + i);
        return result;
    }

    @Override
    public Map<Integer, Map<Sentiment, Integer>> agreementTable(Collection<Map<Integer, Sentiment>> predictedSentiments) {

        Map<Integer, Map<Sentiment, Integer>> result = new HashMap<>();

        int[] p = new int[5];
        int[] n = new int[5];

        for (Map<Integer, Sentiment> myMap : predictedSentiments){
            for (int i=1; i<5; i++){
                Sentiment s = myMap.get(i);

                if (s.equals(Sentiment.POSITIVE))
                    p[i] ++;
                else
                    n[i] ++;
            }
        }

        for (int i=1; i<5; i++){
            Map<Sentiment, Integer> aux = new HashMap<>();
            aux.put(Sentiment.POSITIVE, p[i]);
            aux.put(Sentiment.NEGATIVE, n[i]);
            result.put(i, aux);
        }

        return result;
    }

    @Override
    public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable) {

        double pe = 0, pa = 0;
        double val, curr_total;
        double current_sum;
        double kappa;

        System.out.println("Agreement table size is " + agreementTable.size());

        for (int k = 0; k < 2; k++){

            curr_total = 0;

            for (Map.Entry<Integer, Map<Sentiment, Integer>> myEntry : agreementTable.entrySet()){

                current_sum = myEntry.getValue().getOrDefault(Sentiment.NEGATIVE, 0) + myEntry.getValue().getOrDefault(Sentiment.POSITIVE, 0);

                if (k == 0){
                    val = myEntry.getValue().getOrDefault(Sentiment.POSITIVE, 0);
                }
                else{
                    val = myEntry.getValue().getOrDefault(Sentiment.NEGATIVE, 0);
                }

                curr_total += val / current_sum;
            }

            curr_total /= agreementTable.size();
            curr_total = curr_total * curr_total;

            pe += curr_total;
        }

        System.out.println("PE is equal to " + pe);

        double aux_sum1, aux_sum2, aux_sum;

        for (Map.Entry<Integer, Map<Sentiment, Integer>> myEntry : agreementTable.entrySet()){
            current_sum = myEntry.getValue().getOrDefault(Sentiment.POSITIVE, 0) + myEntry.getValue().getOrDefault(Sentiment.NEGATIVE, 0);
            current_sum = 1 / (current_sum * (current_sum - 1));

            aux_sum1 = myEntry.getValue().getOrDefault(Sentiment.NEGATIVE, 0);
            aux_sum2 = myEntry.getValue().getOrDefault(Sentiment.POSITIVE, 0);

            aux_sum1 = aux_sum1 * (aux_sum1 - 1);
            aux_sum2 = aux_sum2 * (aux_sum2 - 1);

            aux_sum = aux_sum1 + aux_sum2;

            current_sum = aux_sum * current_sum;

            pa += current_sum;
        }

        pa /= agreementTable.size();

        System.out.println("PA is equal to " + pa);

        kappa = (pa - pe) / (1 - pe);

        return kappa;
    }

    public static void main(String[] args) throws IOException {
        Path dataDirectory = Paths.get("C:\\Users\\victo\\Desktop\\DataML\\nuanced_sentiment_dataset");
        Path sentimentFile = dataDirectory.resolve("review_sentiment");
        Map<Path, NuancedSentiment> dataSet = DataPreparation6.loadNuancedDataset(dataDirectory.resolve("reviews"), sentimentFile);
        Exercise6 u = new Exercise6(dataSet);
    }
}
