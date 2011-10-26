package msd.hangman;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

public class WordTankTest {

    @Test(expected = NullPointerException.class)
    public void testNullFile() throws IOException {
        new WordTank(null, NaiveLikelihoodStrategy.FIRST_GUESS);
    }

    @Test(expected = FileNotFoundException.class)
    public void testFileNotFound() throws IOException {
        new WordTank("words-aint-there.txt", NaiveLikelihoodStrategy.FIRST_GUESS);
    }

    private WordTank initializeWordTank() {
        WordTank dict = null;
        try {
            dict = new WordTank("words.txt", NaiveLikelihoodStrategy.FIRST_GUESS);
        } catch (FileNotFoundException e) {
            fail("File not found: " + e.getLocalizedMessage());
        } catch (IOException e) {
            fail("Error while reading or closing file: " + e.getLocalizedMessage());
        }
        
        // Value of `wc -l` against the file.
        assertEquals(173529, dict.getWordCount());
        
        assertTrue(dict.contains(StrategyTester.TEST_WORD));
        for (String word : StrategyTester.SAMPLE_WORDS)
            assertTrue("Does not contain: " + word, dict.contains(word));
        return dict;
    }

    @Test @Ignore
    public void testInitializeUsrFile() {
        WordTank dict = null;
        try {
            long start = System.nanoTime();
            dict = new WordTank("/usr/share/dict/words", NaiveLikelihoodStrategy.FIRST_GUESS);
            System.out.println(System.nanoTime() - start);
        } catch (FileNotFoundException e) {
            fail("File not found: " + e.getLocalizedMessage());
        } catch (IOException e) {
            fail("Error while reading or closing file: " + e.getLocalizedMessage());
        }

        assertTrue(dict.contains(StrategyTester.TEST_WORD));
        // Fails, usr file does not contain OSES.
        for (String word : StrategyTester.SAMPLE_WORDS)
            assertTrue("Does not contain: " + word, dict.contains(word));
    }
    
    @Test
    public void testGetCandidates() {
        WordTank words = initializeWordTank();
        Set<String> candidates = words.getCandidates("-----E");
        assertTrue(candidates.contains(StrategyTester.TEST_WORD));

        for (String word : StrategyTester.SAMPLE_WORDS) {
            candidates = words.getCandidates(word);
            assertTrue(candidates.contains(word));
        }
    }

}
