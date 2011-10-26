package msd.hangman;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for analyzing and processing word sets.
 * 
 * <p>
 * Designed as a package-internal tool and not meant as a public API, so access
 * level has been intentionally set to package private.
 * 
 * @author Michael S. Daines
 */
final class Utilities {

    /**
     * Not meant to be instantiated.
     */
    private Utilities() { }

    /**
     * Filter out words that do not match the predicate.
     * 
     * @param coll
     *            Set of terms to test against the predicate.
     * @param pred
     *            Predicate to test each term.
     * 
     * @return A Set containing all elements that match the supplied predicate.
     */
    static Set<String> filter(Set<String> coll, Predicate pred) {
        HashSet<String> results = new HashSet<String>();
        for (String term : coll)
            if (pred.apply(term))
                results.add(term);
        return results;
    }

    /**
     * Return the most frequently occurring letter in the words in the
     * collection. (Technically, only tracking each letter once per word.) In
     * the case of ties, this algorithm chooses the letter that occurs later in
     * the alphabet. During testing, this appeared to be slightly more efficient
     * and likely to guess earlier.
     * 
     * @param coll
     *            Set of words that are still valid answers to the current game.
     * @param exclude
     *            Set of characters that have already been guessed this game.
     * 
     * @return The character that occurs with the most frequency.
     */
    static char getMostLikelyLetter(Set<String> coll, Set<Character> exclude) {
        int[] frequency = new int['Z' + 1];
        for (String word : coll)
            for (char ch = 'A'; ch <= 'Z'; ch++)
                if (word.indexOf(ch) > -1 && !exclude.contains(ch))
                    frequency[ch]++;
        int max = -1;
        char mostLikely = 'A';
        for (char ch = 'A'; ch <= 'Z'; ch++) {
            max = Math.max(max, frequency[ch]);
            if (frequency[ch] == max)
                mostLikely = ch;
        }
        return mostLikely;
    }

}
