package msd.hangman;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Repository of all known words from which the Hangman game can draw.
 * 
 * <p>
 * This scheme, with the sample word file and using the index character 'E', has
 * the worst case scenario cluster of 8-character words with no 'E' in them
 * (there are 8958 in the sample file, 10762 in the system file).
 * 
 * <p>
 * This class is not thread-safe. It is meant as an package-internal utility
 * class and does not enforce the contract that {@code Set}s it returns are
 * immutable. Intentionally using package default access to partially express
 * this fact.
 * 
 * <p>
 * Internal assumption that no word will be longer than 35 characters (which is
 * the longest non-coined word in a standard dictionary). If a word appears in
 * the input file which is longer than this, an
 * {@code IndexOutOfBoundsException} will occur which can be fixed by increasing
 * the corresponding array's length.
 * 
 * <p>
 * It is also assumed that all words retrieved from this class will be in
 * upper-case and only consist of the 26 standard ASCII letters (A-Z).
 * Characters outside this range or requests for lower-case letters will produce
 * runtime exceptions at best.
 * 
 * @author Michael S. Daines
 */
final class WordTank {

    // The longest word in the sample word file was 29 letters, and 35 is the
    // longest in standard dictionaries.
    private final ByLength[] byLength = new ByLength[36];

    // An index character to use for separating out the word storage. 'E' is the
    // most obvious choice, as it is the most common letter in English, but it
    // seems more appropriate to inject the letter in the constructor to keep
    // that logic external.
    final char indexChar;

    /**
     * Initialize a {@code WordTank} for use by the Hangman game.
     * 
     * <p>
     * The constructor will throw an exception if there is any problem loading
     * the word file, since an instance of the class will have no value if
     * initialization cannot complete.
     * 
     * @param filePath
     *            Valid path to word file that will be loaded into the WordTank.
     * @param indexChar
     *            Character used as the first guess to any word. Character is
     *            used to index the internal storage, so should ideally be a
     *            common letter like 'E', 'I', or 'A'.
     * 
     * @throws IOException
     *             if the file cannot be found, if there is an error while
     *             reading the file, or if the file cannot be closed properly
     *             for some reason.
     */
    WordTank(String filePath, char indexChar) throws IOException {
        if (null == filePath)
            throw new NullPointerException("No word file supplied.");

        this.indexChar = indexChar;
        
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        try {
            String line;
            while ((line = reader.readLine()) != null)
                add(line);
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

    /**
     * Retrieve the subset of words that match word based upon the index of 'E'
     * in the string. Only intended to be called once per match/word.
     * 
     * @param word
     *            Word to use for retrieving subset (e.g., "--TT-E").
     * 
     * @return A subset of the dictionary which may be valid guesses for the
     *         secret word.
     */
    Set<String> getCandidates(String word) {
        ByLength node = getLengthNode(word.length());
        ByLetterIndex letterNode = node.getLetterNode(word.indexOf(indexChar));
        return letterNode.words;
    }

    private void add(String line) {
        String word = line.toUpperCase();
        ByLength lengthNode = getLengthNode(word.length());
        lengthNode.add(word);
    }

    private ByLength getLengthNode(int length) {
        ByLength node = byLength[length];
        if (node == null) {
            byLength[length] = new ByLength(length);
            node = byLength[length];
        }
        return node;
    }

    /*
     * Internal node structure classes.
     */
    
    /**
     * Internal node class to store words based upon their length, as word
     * length is always known in a proper Hangman game.
     */
    private final class ByLength {

        private final ByLetterIndex[] letterIndex;

        private ByLength(int length) {
            // Need to account for -1 as well.
            letterIndex = new ByLetterIndex[length + 2];
        }

        /**
         * Add word to this node.
         * 
         * @param word
         *            to Add. Assumed to be in all upper case at this point.
         */
        private void add(String word) {
            ByLetterIndex letterNode = getLetterNode(word.indexOf(indexChar));
            letterNode.add(word);
        }

        /**
         * Retrieve the letter node based upon the index character.
         * 
         * @param index
         *            the result of {@code String.getIndexOf(char)} on a secret
         *            word.
         * 
         * @return The corresponding letter node.
         */
        private ByLetterIndex getLetterNode(int index) {
            // Because we are accounting for -1 as well, add 1.
            ByLetterIndex node = letterIndex[index + 1];
            if (null == node) {
                letterIndex[index + 1] = new ByLetterIndex();
                node = letterIndex[index + 1];
            }
            return node;
        }

    }

    /**
     * Internal node class to store words based upon an indexed character. This
     * is where the actual {@code Set} of words are stored.
     */
    private final class ByLetterIndex {

        /**
         * NOTE: We are currently being promiscuous with this on the assumption
         * that this class is to only be used in this package in a non-mutating
         * way.
         */
        private final Set<String> words = new HashSet<String>();

        private ByLetterIndex() { }

        private void add(String word) {
            words.add(word);
        }
    }
    
    /*
     * Methods exposed for testing below this point.
     */

    /**
     * Test whether this contains a particular word.
     * 
     * @param word
     *            Word to test presence of.
     * @return true if the word can be found in this dictionary.
     */
    boolean contains(String word) {
        String asUpper = word.toUpperCase();
        ByLength lengthNode = byLength[asUpper.length()];
        ByLetterIndex letterNode = lengthNode.getLetterNode(asUpper.indexOf(indexChar));
        return letterNode.words.contains(asUpper);
    }

    /**
     * Get the total number of words found in this dictionary.
     * 
     * @return Total number of words in dictionary.
     */
    int getWordCount() {
        int wordCount = 0;
        for (ByLength node : byLength)
            if (null != node)
                for (ByLetterIndex lNode : node.letterIndex)
                    if (null != lNode)
                        wordCount += lNode.words.size();
        return wordCount;
    }
}
