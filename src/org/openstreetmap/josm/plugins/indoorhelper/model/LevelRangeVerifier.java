package org.openstreetmap.josm.plugins.indoorhelper.model;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LevelRangeVerifier {
    private static final Pattern RangeRegex =
            Pattern.compile("^(?<LowerBound>-?[0-9]+)-(?<UpperBound>-?[0-9]+)$");

    /**
     * Checks if repeat_on tag value includes current working level value
     *
     * @param repeatOnValue tag as string
     * @param workingLevel  current working level as value
     * @return true if repeat_on tag includes current working level, else false
     */
    public static boolean isPartOfWorkingLevel(String repeatOnValue, int workingLevel) {
        repeatOnValue = repeatOnValue.trim();
        if (repeatOnValue.isEmpty()) {
            return false;
        }

        return Arrays.stream(repeatOnValue.split(";"))
                .anyMatch(x -> isPartOfSingleWorkingLevelGroup(x, workingLevel));
    }

    private static boolean isPartOfSingleWorkingLevelGroup(String repeatOnValue, int workingLevel) {
        Matcher rangeMatch = RangeRegex.matcher(repeatOnValue);

        if (rangeMatch.matches() &&
                rangeMatch.group("LowerBound") != null &&
                rangeMatch.group("UpperBound") != null) {
            boolean fitsLowerBound = tryParseInt(rangeMatch.group("LowerBound"))
                    .map(x -> x <= workingLevel)
                    .orElse(false);
            boolean fitsUpperBound = tryParseInt(rangeMatch.group("UpperBound"))
                    .map(x -> x >= workingLevel)
                    .orElse(false);
            return fitsLowerBound && fitsUpperBound;
        }

        return tryParseInt(repeatOnValue).map(x -> x == workingLevel).orElse(false);
    }

    private static Optional<Integer> tryParseInt(String intString) {
        try {
            return Optional.of(Integer.parseInt(intString));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
