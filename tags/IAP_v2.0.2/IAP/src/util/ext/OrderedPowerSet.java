package util.ext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

// http://stackoverflow.com/questions/20935315/java-generate-all-possible-combinations-of-a-given-list
public class OrderedPowerSet<E> {
	private static final int ELEMENT_LIMIT = 12;
	private final List<E> inputList;
	public int N;
	private final Map<Integer, List<LinkedHashSet<E>>> map =
			new HashMap<Integer, List<LinkedHashSet<E>>>();
	
	public OrderedPowerSet(List<E> list) {
		inputList = list;
		N = list.size();
		if (N > ELEMENT_LIMIT) {
			throw new RuntimeException(
					"List with more then " + ELEMENT_LIMIT + " elements is too long...");
		}
	}
	
	public Map<Integer, List<LinkedHashSet<E>>> getMap() {
		for (int i = 1; i <= N; i++)
			getPermutationsList(i);
		return map;
	}
	
	private List<LinkedHashSet<E>> getPermutationsList(int elementCount) {
		if (elementCount < 1 || elementCount > N) {
			throw new IndexOutOfBoundsException(
					"Can only generate permutations for a count between 1 to " + N);
		}
		if (map.containsKey(elementCount)) {
			return map.get(elementCount);
		}
		
		ArrayList<LinkedHashSet<E>> list = new ArrayList<LinkedHashSet<E>>();
		
		if (elementCount == N) {
			list.add(new LinkedHashSet<E>(inputList));
		} else
			if (elementCount == 1) {
				for (int i = 0; i < N; i++) {
					LinkedHashSet<E> set = new LinkedHashSet<E>();
					set.add(inputList.get(i));
					list.add(set);
				}
			} else {
				list = new ArrayList<LinkedHashSet<E>>();
				for (int i = 0; i <= N - elementCount; i++) {
					@SuppressWarnings("unchecked")
					ArrayList<E> subList = (ArrayList<E>) ((ArrayList<E>) inputList).clone();
					for (int j = i; j >= 0; j--) {
						subList.remove(j);
					}
					OrderedPowerSet<E> subPowerSet =
							new OrderedPowerSet<E>(subList);
					
					List<LinkedHashSet<E>> pList =
							subPowerSet.getPermutationsList(elementCount - 1);
					for (LinkedHashSet<E> s : pList) {
						LinkedHashSet<E> set = new LinkedHashSet<E>();
						set.add(inputList.get(i));
						set.addAll(s);
						list.add(set);
					}
				}
			}
		
		map.put(elementCount, list);
		
		return map.get(elementCount);
	}
}