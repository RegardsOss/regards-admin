/**
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.domain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Couple}
 */
public class CoupleTest {

    Couple<String, Long> couple;

    String first = "Test";

    Long second = 0L;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        couple = new Couple<String, Long>(first, second);
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.accessrights.domain.Couple#Couple()}.
     */
    @Test
    public void testCouple() {
        Couple testCouple = new Couple();
        Assert.assertEquals(null, testCouple.getFirst());
        Assert.assertEquals(null, testCouple.getSecond());
    }

    /**
     * Test method for
     * {@link fr.cnes.regards.modules.accessrights.domain.Couple#Couple(java.lang.Object, java.lang.Object)}.
     */
    @Test
    public void testCoupleParams() {
        String testfirst = "test", testsecond = "test2";
        Couple<String, String> testCouple = new Couple<String, String>(testfirst, testsecond);
        Assert.assertEquals(testfirst, testCouple.getFirst());
        Assert.assertEquals(testsecond, testCouple.getSecond());
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.accessrights.domain.Couple#getFirst()}.
     */
    @Test
    public void testGetFirst() {
        Assert.assertEquals(first, couple.getFirst());
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.accessrights.domain.Couple#setFirst(java.lang.Object)}.
     */
    @Test
    public void testSetFirst() {
        String newFirst = "newFirst";
        couple.setFirst(newFirst);
        Assert.assertEquals(newFirst, couple.getFirst());
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.accessrights.domain.Couple#getSecond()}.
     */
    @Test
    public void testGetSecond() {
        Assert.assertEquals(second, couple.getSecond());
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.accessrights.domain.Couple#setSecond(java.lang.Object)}.
     */
    @Test
    public void testSetSecond() {
        Long newSecond = 2L;
        couple.setSecond(newSecond);
        Assert.assertEquals(newSecond, couple.getSecond());
    }

}