package msd.hangman;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import msd.hangman.StrategyTester.StatBlock;

import org.junit.Ignore;
import org.junit.Test;

import factual.HangmanGame;

public class NaiveLikelihoodStrategyTest {

    @Test
    public void testCattle() throws IOException {
        NaiveLikelihoodStrategy strategy = new NaiveLikelihoodStrategy("words.txt");
        StrategyTester.testWord(strategy);
    }

    @Test @Ignore // This is for testing performance and correctness, not functionality.
    public void scoreSampleWords() throws IOException {
        NaiveLikelihoodStrategy strategy = new NaiveLikelihoodStrategy("words.txt");
        
        StatBlock statistics = StrategyTester.scoreSampleWords(strategy);
        StrategyTester.printResults(statistics);
    }
    
    @Test @Ignore // This is for testing performance and correctness, not functionality.
    public void testPerformance() throws IOException {
        NaiveLikelihoodStrategy strategy = new NaiveLikelihoodStrategy("words.txt");
        
        long totalTime = 0;
        for (int i = 0; i < 1000; i++) {
            StatBlock statistics = StrategyTester.scoreSampleWords(strategy);
            totalTime += statistics.time;
        }
        System.out.println("Average time: " + ((float) totalTime / 1000) + "ms");
        System.out.println("Average time per word: " + ((float) totalTime / 1000 / StrategyTester.SAMPLE_WORDS.length) + "ms");
        // On my machine, getting 46-48ms and ~3ms/word
    }

    @Test @Ignore // This is for testing performance and correctness, not functionality.
    public void analyzeEntireDictionary() throws IOException {
        NaiveLikelihoodStrategy strategy = new NaiveLikelihoodStrategy("words.txt");

        long words = 0;
        long total = 0;
        int worstScore = 0;
        int wrongGuesses = 0;
        int worstGuesses = 0;
        long fails = 0;
        ArrayList<String> poorPerformers = new ArrayList<String>();
        long time = System.currentTimeMillis();
        
        BufferedReader reader = new BufferedReader(new FileReader("words.txt"));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                words++;
                HangmanGame game = new HangmanGame(line.toUpperCase(), 25);
                StrategyTester.testSingleWord(game, strategy);
                total += game.currentScore();
                wrongGuesses += game.numWrongGuessesMade();
                worstScore = Math.max(worstScore, game.currentScore());
                worstGuesses = Math.max(worstGuesses, game.numWrongGuessesMade());
                if (game.numWrongGuessesMade() > 5) {
                    fails++;
                    if (game.numWrongGuessesMade() > 15)
                        poorPerformers.add(line);
                }
            }
        } catch (IOException e) {
            throw new IOException("Error while reading from file.", e);
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new IOException("Error while closing file.", e);
                }
            time = System.currentTimeMillis() - time;
        }
        
        System.out.println("Time to complete entire dictionary: " + time + "ms");
        System.out.println("Average time per word: " + ((float) time / words) + "ms");
        System.out.println("Worst performers (" + poorPerformers.size() + " total): " + poorPerformers);
        System.out.println("Worst score: " + worstScore);
        System.out.println("Worst bad guesses: " + worstGuesses);
        System.out.println("Average score: " + ((float) total / words));
        System.out.println("Average number of wrong guesses: " + ((float) wrongGuesses / words));
        System.out.println("Percentage of words that fail: " + ((float) fails / words * 100) + "%");
    }
}
