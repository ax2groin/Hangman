package msd.hangman;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import factual.GuessingStrategy;
import factual.HangmanGame;

/**
 * A repository for methods and values useful for testing a Strategy. Not meant
 * to be part of a public API.
 * 
 * @author Michael S. Daines
 */
final class StrategyTester {

    /**
     * Sample words from the Factual example.
     */
    static final String[] SAMPLE_WORDS = { "COMAKER", "CUMULATE",
            "ERUPTIVE", "FACTUAL", "MONADISM", "MUS", "NAGGING", "OSES",
            "REMEMBERED", "SPODUMENES", "STEREOISOMERS", "TOXICS",
            "TRICHROMATS", "TRIOSE", "UNIFORMED" };

    /**
     * Test word to use for basic functionality testing throughout the package.
     */
    static final String TEST_WORD = "CATTLE";
    
    /**
     * Test method to do a step-by-step confirmation of a match based upon
     * {@code NaiveLikelihoodStrategy} algorithm.
     * 
     * @param strategy
     *            Strategy to test against.
     */
    static void testWord(GuessingStrategy strategy) {
        HangmanGame game = new HangmanGame(TEST_WORD, 25);

        // Confirm starting state
        assertEquals(HangmanGame.Status.KEEP_GUESSING, game.gameStatus());
        assertEquals(TEST_WORD.length(), game.getSecretWordLength());
        assertEquals(0, game.currentScore());

        strategy.nextGuess(game).makeGuess(game);
        assertEquals("-----E", game.getGuessedSoFar());

        strategy.nextGuess(game).makeGuess(game);
        assertEquals("-----E", game.getGuessedSoFar());
        assertTrue(game.getIncorrectlyGuessedLetters().contains('I'));

        strategy.nextGuess(game).makeGuess(game);
        assertEquals("-A---E", game.getGuessedSoFar());

        strategy.nextGuess(game).makeGuess(game);
        assertEquals("-A--LE", game.getGuessedSoFar());

        strategy.nextGuess(game).makeGuess(game);
        assertEquals("-A--LE", game.getGuessedSoFar());
        assertTrue(game.getIncorrectlyGuessedLetters().contains('D'));

        strategy.nextGuess(game).makeGuess(game);
        assertEquals("-A--LE", game.getGuessedSoFar());
        assertTrue(game.getIncorrectlyGuessedLetters().contains('R'));
        
        strategy.nextGuess(game).makeGuess(game);
        assertEquals("-ATTLE", game.getGuessedSoFar());

        strategy.nextGuess(game).makeGuess(game);
        assertEquals("-ATTLE", game.getGuessedSoFar());
        assertTrue(game.getIncorrectlyGuessedLetters().contains('W'));

        strategy.nextGuess(game).makeGuess(game);

        assertEquals(HangmanGame.Status.GAME_WON, game.gameStatus());
    }
    /**
     * Run a single game of Hangman to completion using the supplied strategy.
     * 
     * @param game
     *            Game (and word) to test against.
     * @param strategy
     *            Strategy to test.
     */
    static void testSingleWord(HangmanGame game, GuessingStrategy strategy) {
        if (game.gameStatus() != HangmanGame.Status.KEEP_GUESSING)
            return;

        while (game.gameStatus() == HangmanGame.Status.KEEP_GUESSING)
            strategy.nextGuess(game).makeGuess(game);
    }

    /**
     * Run the strategy against the sample words given in the example from
     * Factual. Not a proper "testing" method, but a convenient presentation
     * method to visual check against while developing. Using package default
     * level on purpose.
     * 
     * @param strategy
     *            {@code GuessingStrategy} to use for test.
     * 
     * @return Accumulated statistics related to this test run.
     */
    static StatBlock scoreSampleWords(GuessingStrategy strategy) {
        long start = System.currentTimeMillis();
        StatBlock stats = new StatBlock();
        for (String word : StrategyTester.SAMPLE_WORDS) {
            HangmanGame game = new HangmanGame(word, 5);
            StrategyTester.testSingleWord(game, strategy);
            stats.results.put(word, new Result(game.currentScore(), game.numWrongGuessesMade()));
            stats.finalScore += game.currentScore();
        }
        stats.time = System.currentTimeMillis() - start;
        return stats;
    }

    /**
     * Testing class for gathering statistics, so not worried about
     * encapsulation. Using package default level on purpose.
     */
    static class StatBlock {

        long time;

        final Map<String, Result> results = new HashMap<String, Result>();

        int finalScore;

    }

    /**
     * Testing class for gathering statistics, so not worried about
     * encapsulation. Using package default level on purpose.
     */
    static class Result {

        final int score;
        final int wrong;

        private Result(int score, int wrong) {
            this.score = score;
            this.wrong = wrong;
        }
    }

    /**
     * Print the statistics to Standard Out. Not a proper "testing" method, but
     * a convenient presentation method to visual check against while
     * developing. Using package default level on purpose.
     * 
     * @param statistics
     *            Values to print to Standard Out.
     */
    static void printResults(StatBlock statistics) {
        System.out.println(" == Individual Words ==");
        for (Map.Entry<String, Result> entry : statistics.results.entrySet())
            System.out.println(entry.getKey()
                    + " = " + entry.getValue().score
                    + " (" + entry.getValue().wrong + ")");

        System.out.println(" == Overall Results ==");
        // Total score in example sample = 140
        System.out.println("Final Score: " + statistics.finalScore);
        System.out.println("Time to complete: " + statistics.time + "ms");
        System.out.println("Average time per word: " + ((float) statistics.time / statistics.results.size()) + "ms");
    }
}
