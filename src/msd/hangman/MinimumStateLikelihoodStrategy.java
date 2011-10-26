package msd.hangman;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import factual.Guess;
import factual.GuessLetter;
import factual.GuessWord;
import factual.GuessingStrategy;
import factual.HangmanGame;

/**
 * Implementation of the same likelihood strategy as
 * {@code NaiveLikelihoodStrategy}. Unlike the optimized strategy, this stores
 * the dictionary in a plain {@code HashSet} and inefficiently reuses the whole
 * dictionary every time. Not storing any "accidental state" which can be
 * derived from the {@code HangmanGame} at runtime. Uses {@code Pattern} regular
 * expressions to filter out possibilities, which is typically slower than
 * direct parsing using {@code String} methods.
 * 
 * <p>
 * This class is thread-safe.
 * 
 * <p>
 * Except in cases where the most common letter for a word of a given length is
 * not 'E', this strategy should be effectively identical to
 * {@code NaiveLikelihoodStrategy}. As such, it provides a good correctness
 * comparison to the optimizations there.
 * 
 * @author Michael S. Daines
 */
public final class MinimumStateLikelihoodStrategy implements GuessingStrategy {

    private final HashSet<String> dictionary = new HashSet<String>();

    public MinimumStateLikelihoodStrategy(String pathToDictionary)
            throws IOException {
        if (null == pathToDictionary)
            throw new NullPointerException("No dictionary file supplied.");

        BufferedReader reader = new BufferedReader(new FileReader(pathToDictionary));
        try {
            String line;
            while ((line = reader.readLine()) != null)
                dictionary.add(line.toUpperCase());
        } catch (IOException e) {
            throw new IOException("Error while reading from file.", e);
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new IOException("Error while closing file.", e);
                }
        }
    }

    public Guess nextGuess(HangmanGame game) {
        String soFar = game.getGuessedSoFar();

        // Filter down to only words that could match the secret word.
        String regex = getRegex(soFar, game.getAllGuessedLetters());
        final Pattern pattern = Pattern.compile(regex);
        Set<String> potentialWords = Utilities.filter(dictionary,
                new Predicate() {
                    public boolean apply(String term) {
                        return pattern.matcher(term).matches();
                    }
                });

        if (potentialWords.size() == 1)
            for (String word : potentialWords)
                return new GuessWord(word);

        return new GuessLetter(Utilities.getMostLikelyLetter(potentialWords,
                game.getAllGuessedLetters()));
    }

    /*
     * Generate a regular expression that will match length, correct letters,
     * and discard matches against letters that can no longer occur in the given
     * game.
     */
    private static String getRegex(String soFar, Set<Character> tried) {
        String replacement;
        if (tried.isEmpty())
            replacement = ".";
        else {
            // Match only against characters that haven't been tried yet.
            StringBuilder exclude = new StringBuilder("[^");
            for (Character ch : tried)
                exclude.append(ch);
            exclude.append("]");
            replacement = exclude.toString();
        }
        return "^" + soFar.replaceAll(HangmanGame.MYSTERY_LETTER.toString(), replacement) + "$";
    }

}
