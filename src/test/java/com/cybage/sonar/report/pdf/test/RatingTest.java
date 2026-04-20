package com.cybage.sonar.report.pdf.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cybage.sonar.report.pdf.util.Rating;

@Test(groups = { "metrics" })
public class RatingTest {

    @Test
    public void testGetRatingA() {
        Assert.assertEquals(Rating.getRating(Rating.RATING_1), "A");
    }

    @Test
    public void testGetRatingB() {
        Assert.assertEquals(Rating.getRating(Rating.RATING_2), "B");
    }

    @Test
    public void testGetRatingC() {
        Assert.assertEquals(Rating.getRating(Rating.RATING_3), "C");
    }

    @Test
    public void testGetRatingD() {
        Assert.assertEquals(Rating.getRating(Rating.RATING_4), "D");
    }

    @Test
    public void testGetRatingE() {
        Assert.assertEquals(Rating.getRating(Rating.RATING_5), "E");
    }

    @Test
    public void testGetRatingUnknown() {
        Assert.assertNull(Rating.getRating("99.0"));
    }
}
