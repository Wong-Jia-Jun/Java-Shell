package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.exception.RangeHelperException;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The RangeHelper class provides functionality for managing a list of integer ranges.
 * It allows adding, accessing, and checking if a number is within any of the defined ranges.
 */
public class RangeHelper extends AbstractList<int[]> {
    private final List<int[]> ranges = new ArrayList<>();
    public static final String ERR_NULL_RANGES = "Ranges cannot be null";

    public static final String ERR_INVALID_RANGE = "Invalid range";

    /**
     * Constructs a RangeHelper with the specified list of ranges.
     *
     * @param ranges The list of integer ranges.
     * @throws RangeHelperException If the specified ranges are null or invalid.
     */
    public RangeHelper(List<int[]> ranges) throws RangeHelperException {
        if (ranges == null) {
            throw new RangeHelperException(ERR_NULL_RANGES);
        }
        for (int[] range : ranges) {
            if (range.length != 2 || range[0] > range[1]) {
                throw new RangeHelperException(ERR_INVALID_RANGE);
            }
        }
        this.ranges.addAll(ranges);
        this.ranges.sort(Comparator.comparingInt(range -> range[0]));
    }

    /**
     * Retrieves the range at the specified index.
     *
     * @param index The index of the range to retrieve.
     * @return The range at the specified index.
     */
    @Override
    public int[] get(int index) {
        return ranges.get(index);
    }

    /**
     * Retrieves the number of ranges in this RangeHelper.
     *
     * @return The number of ranges.
     */
    @Override
    public int size() {
        return ranges.size();
    }

    /**
     * Adds a new range to the list of ranges.
     *
     * @param range The range to add.
     * @return true (as specified by Collection.add(E)).
     */
    @Override
    public boolean add(int[] range) {
        ranges.add(range);
        ranges.sort(Comparator.comparingInt(range2 -> range2[0]));
        return true;
    }

    /**
     * Checks if the specified number is within any of the ranges.
     *
     * @param number The number to check.
     * @return true if the number is within any of the ranges, false otherwise.
     */
    public boolean contains(int number) {
        for (int[] range : ranges) {
            if (number >= range[0] && number <= range[1]) {
                return true;
            }
        }
        return false;
    }
}


