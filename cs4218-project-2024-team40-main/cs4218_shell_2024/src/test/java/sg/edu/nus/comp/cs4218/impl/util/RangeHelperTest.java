package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.RangeHelperException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The type Range helper test.
 */
public class RangeHelperTest {
    /**
     * The Range helper.
     */
    RangeHelper rangeHelper;

    /**
     * Tear down.
     */
    @AfterEach
    void tearDown() {
        rangeHelper = null;
    }

    /**
     * Range helper given non emptylist sorts elements.
     *
     * @throws RangeHelperException the range helper exception
     */
    @Test
    void rangeHelper_GivenNonEmptylist_SortsElements() throws RangeHelperException {
        ArrayList<int[]> ranges = new ArrayList<>();
        ranges.add(new int[]{3, 4});
        ranges.add(new int[]{2, 7});
        ranges.add(new int[]{1, 2});
        rangeHelper = new RangeHelper(ranges);
        assertEquals(1, rangeHelper.get(0)[0]);
        assertEquals(2, rangeHelper.get(1)[0]);
        assertEquals(3, rangeHelper.get(2)[0]);
    }

    /**
     * Range helper given null list throws exception.
     */
    @Test
    void rangeHelper_GivenNullList_ThrowsException() {
        Throwable exception = assertThrows(RangeHelperException.class, () -> {
            rangeHelper = new RangeHelper(null);
        });
        assertEquals(new RangeHelperException(RangeHelper.ERR_NULL_RANGES).getMessage(),
                exception.getMessage());
    }

    /**
     * Range helper given invalid range start greater than end throws exception.
     */
    @Test
    void rangeHelper_GivenInvalidRangeStartGreaterThanEnd_ThrowsException() {
        ArrayList<int[]> ranges = new ArrayList<>();
        ranges.add(new int[]{3, 4});
        ranges.add(new int[]{2, 1});
        Throwable exception = assertThrows(RangeHelperException.class, () -> {
            rangeHelper = new RangeHelper(ranges);
        });
        assertEquals(new RangeHelperException(RangeHelper.ERR_INVALID_RANGE).getMessage(),
                exception.getMessage());
    }

    /**
     * Range helper given invalid ranges three element range throws exception.
     */
    @Test
    void rangeHelper_GivenInvalidRangesThreeElementRange_ThrowsException() {
        ArrayList<int[]> ranges = new ArrayList<>();
        ranges.add(new int[]{3, 4});
        ranges.add(new int[]{2, 7});
        ranges.add(new int[]{1, 2, 3});
        Throwable exception = assertThrows(RangeHelperException.class, () -> {
            rangeHelper = new RangeHelper(ranges);
        });
        assertEquals(new RangeHelperException(RangeHelper.ERR_INVALID_RANGE).getMessage(),
                exception.getMessage());
    }

    /**
     * Gets given index returns element.
     *
     * @throws RangeHelperException the range helper exception
     */
    @Test
    void get_GivenIndex_ReturnsElement() throws RangeHelperException {
        ArrayList<int[]> ranges = new ArrayList<>();
        ranges.add(new int[]{3, 4});
        ranges.add(new int[]{2, 7});
        ranges.add(new int[]{1, 2});
        rangeHelper = new RangeHelper(ranges);
        assertEquals(1, rangeHelper.get(0)[0]);
        assertEquals(2, rangeHelper.get(1)[0]);
        assertEquals(3, rangeHelper.get(2)[0]);
    }

    /**
     * Size normal case returns size.
     *
     * @throws RangeHelperException the range helper exception
     */
    @Test
    void size_NormalCase_ReturnsSize() throws RangeHelperException {
        ArrayList<int[]> ranges = new ArrayList<>();
        ranges.add(new int[]{3, 4});
        ranges.add(new int[]{2, 7});
        ranges.add(new int[]{1, 2});
        rangeHelper = new RangeHelper(ranges);
        assertEquals(3, rangeHelper.size());
    }

    /**
     * Add normal case adds element.
     *
     * @throws RangeHelperException the range helper exception
     */
    @Test
    void add_NormalCase_AddsElement() throws RangeHelperException {
        ArrayList<int[]> ranges = new ArrayList<>();
        ranges.add(new int[]{3, 4});
        ranges.add(new int[]{2, 7});
        ranges.add(new int[]{1, 2});
        rangeHelper = new RangeHelper(ranges);
        rangeHelper.add(new int[]{5, 6});
        assertEquals(4, rangeHelper.size());
        assertEquals(5, rangeHelper.get(3)[0]);
    }

    /**
     * Contains given number in ranges returns true.
     *
     * @throws RangeHelperException the range helper exception
     */
    @Test
    void contains_GivenNumberInRanges_ReturnsTrue() throws RangeHelperException {
        ArrayList<int[]> ranges = new ArrayList<>();
        ranges.add(new int[]{3, 4});
        ranges.add(new int[]{2, 7});
        ranges.add(new int[]{1, 2});
        rangeHelper = new RangeHelper(ranges);
        assertTrue(rangeHelper.contains(3));
        assertTrue(rangeHelper.contains(4));
        assertTrue(rangeHelper.contains(2));
        assertTrue(rangeHelper.contains(7));
    }

    /**
     * Contains given number not in ranges returns false.
     *
     * @throws RangeHelperException the range helper exception
     */
    @Test
    void contains_GivenNumberNotInRanges_ReturnsFalse() throws RangeHelperException {
        ArrayList<int[]> ranges = new ArrayList<>();
        ranges.add(new int[]{3, 4});
        ranges.add(new int[]{2, 7});
        ranges.add(new int[]{1, 2});
        rangeHelper = new RangeHelper(ranges);
        assertFalse(rangeHelper.contains(0));
        assertFalse(rangeHelper.contains(8));
    }
}
