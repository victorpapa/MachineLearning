package uk.ac.cam.vap32.mlrd.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer.tokenize;

public class Exercise2 implements IExercise2 {

    @Override
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

    @Override
    public Map<String, Map<Sentiment, Double>> calculateUnsmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {

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
                        p = 0.0; n = 1.0;
                        sumN++;
                    }
                    else {
                        p = 1.0; n = 0.0;
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

            double x = pos / sumP ;
            double y = neg / sumN;

            val.put(Sentiment.POSITIVE, (Math.log(x)));
            val.put(Sentiment.NEGATIVE, (Math.log(y)));
        }

        return result;
    }

    @Override
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
}
