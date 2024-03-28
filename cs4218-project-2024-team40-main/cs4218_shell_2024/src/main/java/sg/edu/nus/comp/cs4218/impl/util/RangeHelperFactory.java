package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.exception.RangeHelperException;
import java.util.List;

/**
 * The RangeHelperFactory class is responsible for creating instances of RangeHelper.
 * It provides a method to create a RangeHelper object with the specified list of integer ranges.
 */
public class RangeHelperFactory {

    /**
     * Creates a new RangeHelper object with the specified list of integer ranges.
     *
     * @param ranges The list of integer ranges.
     * @return A new RangeHelper object initialized with the specified ranges.
     * @throws RangeHelperException If the specified ranges are null or invalid.
     */
    public RangeHelper createRangeHelper(List<int[]> ranges) throws RangeHelperException {
        return new RangeHelper(ranges);
    }
}

