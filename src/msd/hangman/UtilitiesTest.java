package msd.hangman;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class UtilitiesTest {

    @Test
    public void testFilter() {
        HashSet<String> base = new HashSet<String>();
        HashSet<String> expected = new HashSet<String>();
        
        for (String word : StrategyTester.SAMPLE_WORDS) {
            base.add(word);
            if (word.contains("U"))
                expected.add(word);
        }
        assertTrue(base.size() > expected.size());

        // Filter for words containing the letter 'U'.
        Set<String> results = Utilities.filter(base, new Predicate() {
            public boolean apply(String term) {
                return term.contains("U");
            }
        });
        assertEquals(expected, results);
        
        // None of the sample words contain the letter 'Z', so check that it returns nothing.
        results = Utilities.filter(base, new Predicate() {
            public boolean apply(String term) {
                return term.contains("Z");
            }
        });
        assertTrue(results.isEmpty());
        
        // Filter that matching everything.
        results = Utilities.filter(base, new Predicate() {
            public boolean apply(String term) {
                return true;
            }
        });
        assertEquals(base, results);
    }

    @Test
    public void testGetMostLikelyLetter() {
        Set<String> samples = new HashSet<String>();
        for (String word : StrategyTester.SAMPLE_WORDS)
            samples.add(word);
        
        // 'O' turns out to be the most likely based on our algorithm and these words.
        char mostLikely = Utilities.getMostLikelyLetter(samples, Collections.<Character>emptySet());
        assertEquals('O', mostLikely);
        
        // Check next best option if 'O' is excluded, which happens to be 'M'.
        Set<Character> noO = new HashSet<Character>();
        noO.add('O');
        mostLikely = Utilities.getMostLikelyLetter(samples, noO);
        assertFalse('O' == mostLikely);
        assertEquals('M', mostLikely);

        // Remove those containing an 'O' and 'U' becomes the most likely.
        samples = Utilities.filter(samples, new Predicate() {
            public boolean apply(String term) {
                return !term.contains("O");
            }
        });
        mostLikely = Utilities.getMostLikelyLetter(samples, Collections.<Character>emptySet());
        assertFalse('O' == mostLikely);
        assertEquals('U', mostLikely);
        
        // When there is nothing, it returns 'Z', since it is the last letter of the alphabet.
        // That's better than crashing.
        mostLikely = Utilities.getMostLikelyLetter(Collections.<String>emptySet(), Collections.<Character>emptySet());
        assertEquals('Z', mostLikely);
    }

}
