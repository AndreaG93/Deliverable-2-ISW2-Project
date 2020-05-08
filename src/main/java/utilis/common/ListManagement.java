package utilis.common;

import java.util.ArrayList;
import java.util.List;

public class ListManagement {

    public static <T> List<List<T>> divideInChunks(List<T> input, int numberOfChunks) {

        List<List<T>> chunks = new ArrayList<>();

        int lowerBound = 0;
        int upperBound;

        do {
            upperBound = Math.min(input.size(), lowerBound + numberOfChunks);

            List<T> chunk = input.subList(lowerBound, upperBound);
            chunks.add(chunk);

            lowerBound += numberOfChunks;

        } while (lowerBound != input.size());


        return chunks;
    }
}
