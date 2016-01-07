package net.pinyin.util;

import java.util.Comparator;

// TODO: Auto-generated Javadoc
/**
 * The Class QuickSort.
 *
 * @param <T> the generic type
 */
public class QuickSort<T> {

    /**
     * Sort.
     *
     * @param values the values
     * @param comparator the comparator
     * @return the t[]
     */
    public T[] sort(T[] values, Comparator<T> comparator) {
        if (values == null || values.length == 0) {
            return values;
        }
        int number = values.length;
        quicksort(values, 0, number - 1, comparator);
        return values;
    }

    /**
     * Quicksort.
     *
     * @param numbers the numbers
     * @param low the low
     * @param high the high
     * @param comparator the comparator
     */
    private void quicksort(T[] numbers, int low, int high, Comparator<T> comparator) {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        T pivot = numbers[(low + high) / 2];

        // Divide into two lists
        while (i <= j) {
            // If the current value from the left list is smaller then the pivot
            // element then get the next element from the left list
            int c = comparator.compare(numbers[i], pivot);
            while (c < 0) {
                i++;
                c = comparator.compare(numbers[i], pivot);
            }
            // If the current value from the right list is larger then the pivot
            // element then get the next element from the right list
            c = comparator.compare(numbers[j], pivot);
            while (c > 0) {
                j--;
                c = comparator.compare(numbers[j], pivot);
            }

            // If we have found a values in the left list which is larger then
            // the pivot element and if we have found a value in the right list
            // which is smaller then the pivot element then we exchange the
            // values.
            // As we are done we can increase i and j
            if (i <= j) {
                exchange(numbers, i, j);
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j) {
            quicksort(numbers, low, j, comparator);
        }
        if (i < high) {
            quicksort(numbers, i, high, comparator);
        }
    }

    /**
     * Exchange.
     *
     * @param numbers the numbers
     * @param i the i
     * @param j the j
     */
    private void exchange(T[] numbers, int i, int j) {
        T temp = numbers[i];
        numbers[i] = numbers[j];
        numbers[j] = temp;
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String args[]) {
        Integer[] array = {1, 2, 5, 4, 3, 9, 7};
        QuickSort<Integer> qs = new QuickSort<Integer>();
        qs.sort(array, new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        for (Integer i : array) {
            System.out.println(i);
        }
    }
}
