package macs.timewasted.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;

public class UtilTest {

	@Test
	void testGetNextMilestone() {
		assertEquals(250000, Util.getNextMilestone(1));
		assertEquals(250000, Util.getNextMilestone(250000));
		assertEquals(500000, Util.getNextMilestone(250001));
		assertEquals(500000, Util.getNextMilestone(423874));
		assertEquals(750000, Util.getNextMilestone(500001));
		assertEquals(1000000, Util.getNextMilestone(750001));
		assertEquals(1500000, Util.getNextMilestone(1000001));
		assertEquals(45500000, Util.getNextMilestone(45000001));
	}
	
	@Test
	void testFormatHourMinute() {
		// testing with 0.5 (30 mins)
		assertNotNull(Util.formatHourMinute(0.5f));
		assertEquals("30 min", Util.formatHourMinute(0.5f));
		
		// testing with 1.39 (1 hour 23 mins)
		assertNotNull(Util.formatHourMinute(1.39f));
		assertEquals("1 hr 23 min", Util.formatHourMinute(1.39f));
		
		// testing with 4,452.92 (4452 hours 55 mins)
		assertNotNull(Util.formatHourMinute(4452.92f));
		assertEquals("4452 hr 55 min", Util.formatHourMinute(4452.92f));
	}
	
	@Test
	void testOrdinal() {
		assertEquals("1st", Util.ordinal(1));
		assertEquals("2nd", Util.ordinal(2));
		assertEquals("3rd", Util.ordinal(3));
		assertEquals("4th", Util.ordinal(4));
		assertEquals("5th", Util.ordinal(5));
		assertEquals("6th", Util.ordinal(6));
		assertEquals("11th", Util.ordinal(11));
		assertEquals("12th", Util.ordinal(12));
		assertEquals("13th", Util.ordinal(13));
		assertEquals("14th", Util.ordinal(14));
		assertEquals("40th", Util.ordinal(40));
		assertEquals("60th", Util.ordinal(60));
		assertEquals("101st", Util.ordinal(101));
		assertEquals("232nd", Util.ordinal(232));
	}
	
	@Test
	void testClamp() {
		assertEquals(0.5, Util.clamp(0.5, 0.0, 1.0), 0.01);
		assertEquals(1.0, Util.clamp(1.5, 0.0, 1.0), 0.01);
		assertEquals(0.0, Util.clamp(-3.0, 0.0, 1.0), 0.01);
		assertEquals(11.2, Util.clamp(11.2, 4.2, 18.9), 0.01);
		assertEquals(4.2, Util.clamp(1.4, 4.2, 18.9), 0.01);
		assertEquals(18.9, Util.clamp(31.2, 4.2, 18.9), 0.01);
		assertEquals(4.2, Util.clamp(4.1, 4.2, 8.9), 0.01);
		assertEquals(4.3, Util.clamp(4.3, 4.2, 8.9), 0.01);
		assertEquals(4.2, Util.clamp(4.2, 4.2, 8.9), 0.01);
		assertEquals(8.9, Util.clamp(8.9, 4.2, 8.9), 0.01);
	}
	
	@Test
	void testTryParseInt() {
		assertEquals(50, Util.tryParseInt("50"));
		assertEquals(27, Util.tryParseInt("27"));
		assertEquals(-30, Util.tryParseInt("-30"));
		assertEquals(0, Util.tryParseInt("50s"));
		assertEquals(0, Util.tryParseInt("abc"));
		assertEquals(0, Util.tryParseInt("28-"));
	}
	
}