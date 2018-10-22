package uk.ac.cam.cl.mlrd.testing;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.DataPreparation1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.utils.DataSplit;
import uk.ac.cam.vap32.mlrd.exercises.Exercise1;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Exercise1Tester {

    static final Path dataDirectory = Paths.get("C:\\Users\\victo\\Desktop\\DataML\\sentiment_dataset");

    public static void main(String[] args) throws IOException {
        Path lexiconFile = Paths.get("C:\\Users\\victo\\Desktop\\DataML\\sentiment_lexicon.txt");

        // Loading the dataset.
        Path sentimentFile = dataDirectory.resolve("review_sentiment");
        Path reviewsDir = dataDirectory.resolve("reviews");
        Map<Path, Sentiment> dataSet = DataPreparation1.loadSentimentDataset(reviewsDir, sentimentFile);

        for (Path myEntry : dataSet.keySet()){
            //System.out.println(myEntry);
        }

        IExercise1 implementation = (IExercise1) new Exercise1();
        DataSplit<Sentiment> mySplit = new DataSplit<>(dataSet, 0);
        System.out.println(mySplit.validationSet.size());

        Map<Path, Sentiment> predictedSentiments = implementation.simpleClassifier(mySplit.validationSet.keySet(), lexiconFile);
        System.out.println("Classifier predictions:");
        System.out.println(predictedSentiments);
        System.out.println();

        double calculatedAccuracy = implementation.calculateAccuracy(dataSet, predictedSentiments);
        System.out.println("Classifier accuracy:");
        System.out.println(calculatedAccuracy);
        System.out.println();

        Map<Path, Sentiment> improvedPredictions = implementation.improvedClassifier(dataSet.keySet(), lexiconFile);
        System.out.println("Improved classifier predictions:");
        System.out.println(improvedPredictions);
        System.out.println();

        System.out.println("Improved classifier accuracy:");
        System.out.println(implementation.calculateAccuracy(dataSet, improvedPredictions));
        System.out.println();
    }
}