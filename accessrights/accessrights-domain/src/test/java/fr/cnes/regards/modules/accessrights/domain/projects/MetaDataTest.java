/**
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.domain.projects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.cnes.regards.modules.accessrights.domain.UserVisibility;

/**
 * Unit testing of {@link MetaData}
 */
public class MetaDataTest {

    /**
     * Test MetaData
     */
    private MetaData metaData;

    /**
     * Test id
     */
    private final Long id = 0L;

    /**
     * Test key
     */
    private final String key = "key";

    /**
     * Test value
     */
    private final String value = "val";

    /**
     * Test UserVisibility value
     */
    private final UserVisibility visibility = UserVisibility.READABLE;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() {
        metaData = new MetaData();
        metaData.setId(id);
        metaData.setKey(key);
        metaData.setValue(value);
        metaData.setVisibility(visibility);
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.accessrights.domain.projects.MetaData#MetaData()}.
     */
    @Test
    public void testMetaData() {
        final MetaData meta = new MetaData();
        Assert.assertEquals(null, meta.getId());
        Assert.assertEquals(null, meta.getKey());
        Assert.assertEquals(null, meta.getValue());
        Assert.assertEquals(null, meta.getVisibility());
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.accessrights.domain.projects.MetaData#getId()}.
     */
    @Test
    public void testGetId() {
        Assert.assertEquals(id, metaData.getId());
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.accessrights.domain.projects.MetaData#setId(java.lang.Long)}.
     */
    @Test
    public void testSetId() {
        final Long newId = 4L;
        metaData.setId(newId);
        Assert.assertEquals(newId, metaData.getId());
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.accessrights.domain.projects.MetaData#getKey()}.
     */
    @Test
    public void testGetKey() {
        Assert.assertEquals(key, metaData.getKey());
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.accessrights.domain.projects.MetaData#setKey(java.lang.String)}.
     */
    @Test
    public void testSetKey() {
        final String newKey = "newKey";
        metaData.setKey(newKey);
        Assert.assertEquals(newKey, metaData.getKey());
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.accessrights.domain.projects.MetaData#getValue()}.
     */
    @Test
    public void testGetValue() {
        Assert.assertEquals(value, metaData.getValue());
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.accessrights.domain.projects.MetaData#setValue(java.lang.String)}.
     */
    @Test
    public void testSetValue() {
        final String newValue = "newValue";
        metaData.setValue(newValue);
        Assert.assertEquals(newValue, metaData.getValue());
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.accessrights.domain.projects.MetaData#getVisibility()}.
     */
    @Test
    public void testGetVisibility() {
        Assert.assertEquals(visibility, metaData.getVisibility());
    }

    /**
     * Test method for
     * {@link fr.cnes.regards.modules.accessrights.domain.projects.MetaData#setVisibility(fr.cnes.regards.modules.accessrights.domain.UserVisibility)}.
     */
    @Test
    public void testSetVisibility() {
        metaData.setVisibility(UserVisibility.HIDDEN);
        Assert.assertEquals(UserVisibility.HIDDEN, metaData.getVisibility());
    }

}