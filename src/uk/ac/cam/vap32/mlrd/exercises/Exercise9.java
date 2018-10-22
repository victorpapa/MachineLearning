package uk.ac.cam.vap32.mlrd.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Exercise9 implements IExercise9 {
    @Override
    public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs) throws IOException {
        Map<Feature, Map<Feature, Double>> transitionMatrix = new HashMap<>();
        Map<Feature, Map<Feature, Double>> countMatrix = new HashMap<>();
        Map<Feature, Map<AminoAcid, Double>> emissionMatrix = new HashMap<>();
        Map<Feature, Map<AminoAcid, Double>> emissionProbs = new HashMap<>();
        Map<Feature, Double> stateCount = new HashMap<>();

        HMMDataStore<AminoAcid, Feature> myData = null;

        for (HMMDataStore<AminoAcid, Feature> entry : sequencePairs) {

            if (myData != null) {
                myData.observedSequence.addAll(entry.observedSequence);
                myData.hiddenSequence.addAll(entry.hiddenSequence);
            } else {

                List<AminoAcid> aux1 = new ArrayList<>();
                aux1.addAll(entry.observedSequence);
                List<Feature> aux2 = new ArrayList<>();
                aux2.addAll(entry.hiddenSequence);

                myData = new HMMDataStore<>(aux1, aux2);
            }
        }

        int count = -1;

        Feature[] types = new Feature[5];

        for (Feature f : Feature.values()){
            types[++count] = f;
        }

        count = -1;

        AminoAcid[] rollTypes = new AminoAcid[23];

        for (AminoAcid a : AminoAcid.values()){
            rollTypes[++count] = a;
        }

        Feature prevType = myData.hiddenSequence.get(0);
        stateCount.put(prevType, 1.0);

        Map<AminoAcid, Double> aux2 = new HashMap<>();
        aux2.put(AminoAcid.START, 1.0);

        emissionProbs.put(Feature.START, aux2);

        for (int i=1; i<myData.observedSequence.size(); i++) {

            Feature type = myData.hiddenSequence.get(i);
            AminoAcid roll = myData.observedSequence.get(i);

            if (stateCount.get(type) == null){
                stateCount.put(type, 1.0);
            }
            else{
                stateCount.put(type, stateCount.get(type) + 1);
            }

            if (emissionProbs.get(type) == null){
                Map<AminoAcid, Double> aux = new HashMap<>();
                aux.put(roll, 1.0);

                emissionProbs.put(type, aux);
            }
            else{
                Map<AminoAcid, Double> aux = emissionProbs.get(type);

                double x = aux.getOrDefault(roll, 0.0);
                emissionProbs.get(type).put(roll, x+1);
            }

            if (prevType == Feature.END){
                prevType = type;
                continue;
            }

            if (countMatrix.get(prevType) == null){

                Map<Feature, Double> aux = new HashMap<>();
                aux.put(type, 1.0);

                countMatrix.put(prevType, aux);
            }
            else{
                Map<Feature, Double> aux = countMatrix.get(prevType);
                double x = aux.getOrDefault(type, 0.0);
                aux.put(type, x+1);
            }

            prevType = type;
        }

        for (int i=0; i<types.length; i++){
            Feature type = types[i];

            Map<AminoAcid, Double> aux = emissionProbs.get(type);
            if (aux == null)
                aux = new HashMap<>();

            for (int j=0; j<rollTypes.length; j++){
                AminoAcid roll = rollTypes[j];

                double value = aux.getOrDefault(roll, 0.0);
                double number = value / stateCount.get(type);

                aux.put(roll, number);
            }
        }

        for (int i=0; i<types.length; i++) {
            Feature type = types[i];
            Map<AminoAcid, Double> aux = new HashMap<>();

            for (int j=0; j<rollTypes.length; j++){

                AminoAcid roll = rollTypes[j];

                if (emissionProbs.get(type) != null)
                    aux.put(roll, emissionProbs.get(type).getOrDefault(roll, 0.0));
                else
                    aux.put(roll, 0.0);
            }

            emissionMatrix.put(type, aux);
        }

        for (Feature F : countMatrix.keySet()){
            Map<Feature, Double> aux = new HashMap<>();
            double value;
            double sum = 0;

            for (Feature L : countMatrix.get(F).keySet()){
                value = countMatrix.get(F).get(L);
                sum += value;
            }

            for (Feature L : countMatrix.get(F).keySet()){
                value = countMatrix.get(F).get(L) / sum;
                aux.put(L, value);
                transitionMatrix.put(F, aux);
            }
        }
        for (Feature F : types){

            if (transitionMatrix.get(F) == null){
                Map<Feature, Double> aux = new HashMap<>();

                for (Feature f : Feature.values()){
                    aux.put(f, 0.0);
                }

                transitionMatrix.put(F, aux);
            }
            else {
                for (Feature L : types) {
                    if (transitionMatrix.get(F).get(L) == null)
                        transitionMatrix.get(F).put(L, 0.0);
                }
            }
        }

        for (Feature f : transitionMatrix.keySet()){
            Map<Feature, Double> aux = transitionMatrix.get(f);

            System.out.print(f + ": ");

            for (Feature l : aux.keySet()){
                System.out.print(l + " -> " + aux.get(l) + ";  ");
            }

            System.out.println();
        }

        System.out.println();System.out.println();System.out.println();

        for (Feature f : emissionMatrix.keySet()){
            Map<AminoAcid, Double> aux = emissionMatrix.get(f);

            System.out.print(f + ": ");

            for (AminoAcid a : aux.keySet()){
                System.out.print(a + " -> " + aux.get(a) + ";  ");
            }

            System.out.println();
        }

        return new HiddenMarkovModel<>(transitionMatrix, emissionMatrix);
    }

    @Override
    public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, List<AminoAcid> observedSequence) {
        List<Map<Feature, Double>> dp =  new ArrayList<>();
        List<Map<Feature, Feature>> best = new ArrayList<>();
        Map<Feature, Double> aux = new HashMap<>();
        Map<Feature, Feature> aux2 = new HashMap<>();
        Map<Feature, Map<AminoAcid, Double>> emissionMatrix = model.getEmissionMatrix();
        Map<Feature, Map<Feature, Double>> transitionMatrix = model.getTransitionMatrix();
        List<Map<Feature, Double>> extra = new ArrayList<>();
        Map<Feature, Double> extraAux = new HashMap<>();

        List<Feature> seqOfBest = new ArrayList<>();

        double prob, extraProb;
        Feature argmax = null;
        Feature extraMax = null;

        Feature[] types = new Feature[3];
        Feature[] types2 = types;
        types[0] = Feature.INSIDE;
        types[1] = Feature.OUTSIDE;
        types[2] = Feature.MEMBRANE;

        aux.put(Feature.INSIDE, Math.log(1.0));
        aux.put(Feature.OUTSIDE, Math.log(1.0));
        aux.put(Feature.MEMBRANE, Math.log(1.0));
        dp.add(aux);

        aux2.put(Feature.INSIDE, Feature.START);
        aux2.put(Feature.OUTSIDE, Feature.START);
        aux2.put(Feature.MEMBRANE, Feature.START);
        best.add(aux2);

        extraAux.put(Feature.START, Math.log(1.0));
        extra.add(extraAux);

        for (int t = 1; t < observedSequence.size(); t++){

            aux = new HashMap<>();
            aux2 = new HashMap<>();
            extraAux = new HashMap<>();

            if (t == observedSequence.size() - 1){
                types2 = new Feature[]{Feature.END};
            }

            for (Feature type : types2) {

                prob = -999999;
                extraProb = -999999;

                for (Feature i : types) {

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
                        extraGamma = extra.get(t-1).get(Feature.START);
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

        List<Feature> result = new ArrayList<>();
        for (int i=0; i<dp.size(); i++)
            result.add(Feature.INSIDE);

        result.set(0, Feature.START);
        result.set(dp.size() - 1, Feature.END);

        Feature value = Feature.END;

        for (int i=dp.size() - 1; i>=1; i--){
            result.set(i, value);
            value = best.get(i).get(value);
        }

        return result;
    }

    @Override
    public Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model, List<HMMDataStore<AminoAcid, Feature>> testSequencePairs) throws IOException {
        HMMDataStore<AminoAcid, Feature> myData;

        Map<List<Feature>, List<Feature>> result = new HashMap<>();
        List<Feature> aux;

        for (HMMDataStore<AminoAcid, Feature> entry : testSequencePairs) {

            aux = viterbi(model, entry.observedSequence);

            result.put(entry.hiddenSequence, aux);
        }

        return result;
    }

    @Override
    public double precision(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double result = 0;
        double total = 0;

        for (Map.Entry<List<Feature>, List<Feature>> myEntry : true2PredictedMap.entrySet()){
            List<Feature> trueList = myEntry.getKey();
            List<Feature> predictedList = myEntry.getValue();

            for (int i=0; i<trueList.size(); i++){
                if (predictedList.get(i) == Feature.MEMBRANE){
                    total ++;

                    if (trueList.get(i) == Feature.MEMBRANE){
                        result ++;
                    }
                }
            }
        }

        result /= total;

        return result;
    }

    @Override
    public double recall(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double result = 0;
        double total = 0;

        for (Map.Entry<List<Feature>, List<Feature>> myEntry : true2PredictedMap.entrySet()){
            List<Feature> trueList = myEntry.getKey();
            List<Feature> predictedList = myEntry.getValue();

            for (int i=0; i<trueList.size(); i++){
                if (trueList.get(i) == Feature.MEMBRANE){
                    total ++;

                    if (predictedList.get(i) == Feature.MEMBRANE){
                        result ++;
                    }
                }
            }
        }

        result /= total;

        return result;
    }

    @Override
    public double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double p = precision(true2PredictedMap);
        double r = recall(true2PredictedMap);

        return 2 * (p*r) / (p+r);
    }
}
