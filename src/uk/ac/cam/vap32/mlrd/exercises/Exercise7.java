package uk.ac.cam.vap32.mlrd.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Exercise7 implements IExercise7 {
    @Override
    public HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException {

        Map<DiceType, Map<DiceType, Double>> transitionMatrix = new HashMap<>();
        Map<DiceType, Map<DiceType, Double>> countMatrix = new HashMap<>();
        Map<DiceType, Map<DiceRoll, Double>> emissionMatrix = new HashMap<>();
        Map<DiceType, Map<DiceRoll, Double>> emissionProbs = new HashMap<>();
        Map<DiceType, Double> stateCount = new HashMap<>();

        HMMDataStore<DiceRoll, DiceType> myData = null;

        for (Path p : sequenceFiles) {

            if (myData != null) {
                HMMDataStore<DiceRoll, DiceType> currentData = HMMDataStore.loadDiceFile(p);
                myData.observedSequence.addAll(currentData.observedSequence);
                myData.hiddenSequence.addAll(currentData.hiddenSequence);
            } else
                myData = HMMDataStore.loadDiceFile(p);
        }


            DiceType[] types = new DiceType[4];
            types[0] = DiceType.START;
            types[1] = DiceType.FAIR;
            types[2] = DiceType.WEIGHTED;
            types[3] = DiceType.END;

            DiceRoll[] rollTypes = new DiceRoll[8];
            rollTypes[0] = DiceRoll.START;
            rollTypes[1] = DiceRoll.ONE;
            rollTypes[2] = DiceRoll.TWO;
            rollTypes[3] = DiceRoll.THREE;
            rollTypes[4] = DiceRoll.FOUR;
            rollTypes[5] = DiceRoll.FIVE;
            rollTypes[6] = DiceRoll.SIX;
            rollTypes[7] = DiceRoll.END;

            DiceType prevType = myData.hiddenSequence.get(0);
            stateCount.put(prevType, 1.0);

            Map<DiceRoll, Double> aux2 = new HashMap<>();
            aux2.put(DiceRoll.START, 1.0);

            emissionProbs.put(DiceType.START, aux2);

            for (int i=1; i<myData.observedSequence.size(); i++) {

                DiceType type = myData.hiddenSequence.get(i);
                DiceRoll roll = myData.observedSequence.get(i);

                if (stateCount.get(type) == null){
                    stateCount.put(type, 1.0);
                }
                else{
                    stateCount.put(type, stateCount.get(type) + 1);
                }

                if (emissionProbs.get(type) == null){
                    Map<DiceRoll, Double> aux = new HashMap<>();
                    aux.put(roll, 1.0);

                    emissionProbs.put(type, aux);
                }
                else{
                    Map<DiceRoll, Double> aux = emissionProbs.get(type);

                    double x = aux.getOrDefault(roll, 0.0);
                    emissionProbs.get(type).put(roll, x+1);
                }

                if (prevType == DiceType.END){
                    prevType = DiceType.START;
                    continue;
                }

                if (countMatrix.get(prevType) == null){

                    Map<DiceType, Double> aux = new HashMap<>();
                    aux.put(type, 1.0);

                    countMatrix.put(prevType, aux);
                }
                else{
                    Map<DiceType, Double> aux = countMatrix.get(prevType);
                    double x = aux.getOrDefault(type, 0.0);
                    aux.put(type, x+1);
                }

                prevType = type;
            }

            /*for (int i=0; i<4; i++){
                System.out.println(stateCount.get(types[i]));
            }*/

            for (int i=0; i<4; i++){
                DiceType type = types[i];

                Map<DiceRoll, Double> aux = emissionProbs.get(type);
                if (aux == null)
                    aux = new HashMap<>();

                for (int j=0; j<8; j++){
                    DiceRoll roll = rollTypes[j];

                    double value = aux.getOrDefault(roll, 0.0);
                    double number = value / stateCount.get(type);

                    aux.put(roll, number);
                }
            }

            for (int i=0; i<types.length; i++) {
                DiceType type = types[i];
                Map<DiceRoll, Double> aux = new HashMap<>();

                for (int j=0; j<rollTypes.length; j++){

                    DiceRoll roll = rollTypes[j];

                    if (emissionProbs.get(type) != null)
                        aux.put(roll, emissionProbs.get(type).getOrDefault(roll, 0.0));
                    else
                        aux.put(roll, 0.0);
                }

                emissionMatrix.put(type, aux);
            }

            for (DiceType F : countMatrix.keySet()){
                Map<DiceType, Double> aux = new HashMap<>();
                double value;
                double sum = 0;

                for (DiceType L : countMatrix.get(F).keySet()){
                    value = countMatrix.get(F).get(L);
                    sum += value;
                }

                for (DiceType L : countMatrix.get(F).keySet()){
                    value = countMatrix.get(F).get(L) / sum;
                    aux.put(L, value);
                    transitionMatrix.put(F, aux);
                }
            }

            //System.out.println(stateCount.get(DiceType.START));
            //System.out.println(emissionProbs.get(DiceType.START).get(DiceRoll.START));

            for (DiceType F : types){

                //System.out.print(F + ": ");

                for (DiceType L : types){
                    if (transitionMatrix.get(F) == null){
                        Map<DiceType, Double> aux = new HashMap<>();

                        aux.put(DiceType.START, 0.0);
                        aux.put(DiceType.FAIR, 0.0);
                        aux.put(DiceType.WEIGHTED, 0.0);
                        aux.put(DiceType.END, 0.0);

                        transitionMatrix.put(F, aux);
                    }

                    if (transitionMatrix.get(F).get(L) == null)
                        transitionMatrix.get(F).put(L, 0.0);

                    //System.out.print(transitionMatrix.get(F).get(L) + " ");
                }

                //System.out.println();
            }

            //System.out.println();System.out.println();

        return new HiddenMarkovModel<>(transitionMatrix, emissionMatrix);
    }
}
