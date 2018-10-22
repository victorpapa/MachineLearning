package uk.ac.cam.vap32.mlrd.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Exercise8 implements IExercise8 {
    @Override
    public List<DiceType> viterbi(HiddenMarkovModel<DiceRoll, DiceType> model, List<DiceRoll> observedSequence) {

        List<Map<DiceType, Double>> dp =  new ArrayList<>();
        List<Map<DiceType, DiceType>> best = new ArrayList<>();
        Map<DiceType, Double> aux = new HashMap<>();
        Map<DiceType, DiceType> aux2 = new HashMap<>();
        Map<DiceType, Map<DiceRoll, Double>> emissionMatrix = model.getEmissionMatrix();
        Map<DiceType, Map<DiceType, Double>> transitionMatrix = model.getTransitionMatrix();
        List<Map<DiceType, Double>> extra = new ArrayList<>();
        Map<DiceType, Double> extraAux = new HashMap<>();

        List<DiceType> seqOfBest = new ArrayList<>();

        double prob, extraProb;
        DiceType argmax = null;
        DiceType extraMax = null;

        DiceType[] types = new DiceType[2];
        DiceType[] types2 = types;
        types[0] = DiceType.FAIR;
        types[1] = DiceType.WEIGHTED;

        aux.put(DiceType.FAIR, Math.log(1.0));
        aux.put(DiceType.WEIGHTED, Math.log(1.0));
        dp.add(aux);

        aux2.put(DiceType.FAIR, DiceType.START);
        aux2.put(DiceType.WEIGHTED, DiceType.START);
        best.add(aux2);

        extraAux.put(DiceType.START, Math.log(1.0));
        extra.add(extraAux);

        for (int t = 1; t < observedSequence.size(); t++){

            aux = new HashMap<>();
            aux2 = new HashMap<>();
            extraAux = new HashMap<>();

            if (t == observedSequence.size() - 1){
                types2 = new DiceType[]{DiceType.END};
            }

            for (DiceType type : types2) {

                prob = -999999;
                extraProb = -999999;

                for (DiceType i : types) {

                    double gamma = dp.get(t - 1).get(i);
                    double alpha = transitionMatrix.get(i).get(type);
                    double beta = emissionMatrix.get(type).get(observedSequence.get(t));

                    double val = gamma + Math.log(alpha) + Math.log(beta);

                    if (val > prob){
                        prob = val;
                        argmax = i;
                    }

                    double extraGamma;

                    if (t == 1){
                        extraGamma = extra.get(t-1).get(DiceType.START);
                    }
                    else{
                        extraGamma = extra.get(t-1).get(i);
                    }

                    val = extraGamma + Math.log(alpha) + Math.log(beta);

                    if (val > extraProb){
                        extraProb = val;
                        extraMax = i;
                    }
                }

                aux.put(type, prob);
                aux2.put(type, argmax);
                extraAux.put(type, extraProb);
            }

            dp.add(aux);
            best.add(aux2);
            extra.add(extraAux);

            seqOfBest.add(extraMax);
        }

        //System.out.println("Sequence of best:");System.out.print("[S, ");for (int i=0; i<seqOfBest.size(); i++){ System.out.print(seqOfBest.get(i) + ", "); }System.out.println("E]"); System.out.println();

        List<DiceType> result = new ArrayList<>();
        for (int i=0; i<dp.size(); i++)
            result.add(DiceType.FAIR);

        result.set(0, DiceType.START);
        result.set(dp.size() - 1, DiceType.END);

        DiceType value = DiceType.END;

        for (int i=dp.size() - 1; i>=1; i--){
            result.set(i, value);
            value = best.get(i).get(value);
        }

        return result;
    }

    @Override
    public Map<List<DiceType>, List<DiceType>> predictAll(HiddenMarkovModel<DiceRoll, DiceType> model, List<Path> testFiles) throws IOException {
        HMMDataStore<DiceRoll, DiceType> myData;

        Map<List<DiceType>, List<DiceType>> result = new HashMap<>();
        List<DiceType> aux;

        for (Path p : testFiles) {

            myData = HMMDataStore.loadDiceFile(p);

            aux = viterbi(model, myData.observedSequence);

            result.put(myData.hiddenSequence, aux);
        }

        return result;
    }

    @Override
    public double precision(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {

        double result = 0;
        double total = 0;

        for (Map.Entry<List<DiceType>, List<DiceType>> myEntry : true2PredictedMap.entrySet()){
            List<DiceType> trueList = myEntry.getKey();
            List<DiceType> predictedList = myEntry.getValue();

            for (int i=0; i<trueList.size(); i++){
                if (predictedList.get(i) == DiceType.WEIGHTED){
                    total ++;

                    if (trueList.get(i) == DiceType.WEIGHTED){
                        result ++;
                    }
                }
            }
        }

        result /= total;

        return result;
    }

    @Override
    public double recall(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double result = 0;
        double total = 0;

        for (Map.Entry<List<DiceType>, List<DiceType>> myEntry : true2PredictedMap.entrySet()){
            List<DiceType> trueList = myEntry.getKey();
            List<DiceType> predictedList = myEntry.getValue();

            for (int i=0; i<trueList.size(); i++){
                if (trueList.get(i) == DiceType.WEIGHTED){
                    total ++;

                    if (predictedList.get(i) == DiceType.WEIGHTED){
                        result ++;
                    }
                }
            }
        }

        result /= total;

        return result;
    }

    @Override
    public double fOneMeasure(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {

        double p = precision(true2PredictedMap);
        double r = recall(true2PredictedMap);

        return 2 * (p*r) / (p+r);
    }
}
