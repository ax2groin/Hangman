package msd.hangman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import factual.Guess;
import factual.GuessLetter;
import factual.GuessWord;
import factual.GuessingStrategy;
import factual.HangmanGame;

/**
 * Hangman strategy that always chooses the letter most likely to occur based
 * upon a naive frequency check against the dictionary. Because the letter 'E'
 * occurs most frequently in the English language, this strategy has been
 * short-circuited to always choose that letter first. (In sampled dictionaries,
 * 'E' occurs in 65-70% of all words.)
 * 
 * <p>
 * This class is not thread-safe.
 * 
 * <p>
 * This strategy is deterministic, such that it will always behave the same
 * against the same word. It will always fail against certain words. Analysis
 * has shown that failure is most likely on 3-5 letter words that easily change
 * to another work with only one letter change (e.g., "bill", "dill", "fill",
 * etc.).
 * 
 * <p>
 * There are state assumptions in this strategy, and it is not built to
 * withstand malicious abuse.
 * 
 * @author Michael S. Daines
 */
public final class NaiveLikelihoodStrategy implements GuessingStrategy {

    // This strategy always guesses 'E' first, since it is the most common
    // letter in English.
    static final char FIRST_GUESS = 'E';
    private static final GuessLetter firstGuess = new GuessLetter(FIRST_GUESS);

    private final WordTank dictionary;

    /*
     * Some variables to track current state.
     */

    // Object reference to detect if a new game has started.
    private HangmanGame currentGame;

    // boolean to determine whether to start with FIRST_GUESS.
    private boolean firstAttempt = true;

    // Known subset of the dictionary that could still be the answer.
    private Set<String> potentialWords;

    // Last character guesses, so that we can optimize our filtering.
    private char lastGuess;

    /**
     * Initialize this Strategy to prepare for a game of Hangman. The strategy
     * can serially play any number of games of Hangman, but can only play one
     * at a time.
     * 
     * @param pathToDictionary
     *            Valid path to a dictionary file that is the source of all
     *            legal words for the game.
     * 
     * @throws IOException
     *             if the file path passed in does not resolve to a valid file
     *             or words cannot be read out of the file for any reason.
     */
    public NaiveLikelihoodStrategy(String pathToDictionary) throws IOException {
        dictionary = new WordTank(pathToDictionary, FIRST_GUESS);
    }

    public Guess nextGuess(HangmanGame game) {
        // Currently making the assumption that the game passed in has only been
        // handled by this strategy. Only using object reference to check this,
        // so could easily break if a new "game" is instantiated with each
        // guess.
        if (currentGame != game)
            initializeState(game);
        
        if (firstAttempt) {
            firstAttempt = false;
            return firstGuess;
        } else {
            String soFar = game.getGuessedSoFar();
            
            if (null == potentialWords) {
                potentialWords = dictionary.getCandidates(soFar);
                
                // And we can assume at this point that the last guess was the
                // first with the letter 'E'.
                if (soFar.indexOf('E') == soFar.lastIndexOf('E')) {
                    potentialWords = Utilities.filter(potentialWords,
                            new Predicate() {
                                public boolean apply(String term) {
                                    return term.indexOf('E') == term.lastIndexOf('E');
                                }
                            });
                } else {
                    potentialWords = filterOutImpossibles(soFar, soFar.indexOf('E') + 1);
                }
            } else if (soFar.indexOf(lastGuess) == -1) {
                potentialWords = Utilities.filter(potentialWords,
                        new Predicate() {
                            public boolean apply(String term) {
                                return term.indexOf(lastGuess) == -1;
                            }
                        });
            } else {
                potentialWords = filterOutImpossibles(soFar, 0);
            }

            // TODO: There may be room for word-guess optimization here based
            // upon remaining words. For example, if there's only one wrong
            // guess left or the options are small.
            
            // If there's only one choice left, just guess it
            if (potentialWords.size() == 1)
                for (String word : potentialWords)
                    return new GuessWord(word);
            
            lastGuess = Utilities.getMostLikelyLetter(potentialWords, game.getAllGuessedLetters());
            return new GuessLetter(lastGuess);
        }
    }

    /**
     * Run this (or any) strategy against a game until completion.
     * 
     * <p>
     * I implemented this here as per the instructions. In my tests, I use the
     * roughly equivalent method in {@code StrategyTester}.
     * 
     * @param game
     *            Initialized game with the secret word set and ready to start.
     * @param strategy
     *            A valid strategy which will attempt to solve the secret word.
     * 
     * @return The final score of the game.
     */
    public static int run(HangmanGame game, GuessingStrategy strategy) {
        if (game.gameStatus() != HangmanGame.Status.KEEP_GUESSING)
            return game.currentScore();

        while (game.gameStatus() == HangmanGame.Status.KEEP_GUESSING)
            strategy.nextGuess(game).makeGuess(game);
        
        return game.currentScore();
    }

    /**
     * Clean up mutable state if it looks like a new game has started.
     */
    private void initializeState(HangmanGame game) {
        currentGame = game;
        firstAttempt = true;
        potentialWords = null;
        lastGuess = FIRST_GUESS; 
    }

    /**
     * Get a subset of the collection that can still potentially match the
     * secret word. This method works because we narrow down the results with
     * each pass, so we only need to check for the presence or absence of the
     * {@code lastGuess}, which keeps us from using a heavier-weight regular
     * expression.
     * 
     * @param soFar
     *            Current representation of the Hangman word.
     * @param start
     *            Position in the string to start checking. Checking against 'E'
     *            doesn't need to start at 0, since the {@code WordTank} has
     *            already optimized for that.
     */
    private Set<String> filterOutImpossibles(String soFar, int start) {
        final List<Check> toCheck = getPositions(soFar, lastGuess, start);
        return Utilities.filter(potentialWords,
                new Predicate() {
                    public boolean apply(String term) {
                        for (Check check : toCheck) {
                            if (term.charAt(check.index) != lastGuess && check.present)
                                return false;
                            if (term.charAt(check.index) == lastGuess && !check.present)
                                return false;
                        }
                        return true;
                    }
                });
    }

    /**
     * We are only concerned with the {@code lastGuess}, since other letters and
     * positions have already been filtered for.
     */
    private static List<Check> getPositions(String soFar, char check, int position) {
        ArrayList<Check> toCheck = new ArrayList<Check>();
        for (int i = 0; i < soFar.length(); i++) {
            char ch = soFar.charAt(i);
            if (ch == HangmanGame.MYSTERY_LETTER)
                toCheck.add(new Check(i, false));
            else if (ch == check)
                toCheck.add(new Check(i, true));
        }
        return toCheck;
    }
    
    /**
     * Internal utility class to simulate a letter versus not letter regular expression. 
     */
    private static final class Check {
        
        private final int index;
        private final boolean present;
        
        private Check(int index, boolean present) {
            this.index = index;
            this.present = present;
        }
    }

}
