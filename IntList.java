import java.util.*;

public interface IntList {
	public int size();
	public void add(int value);
	public int getInt(int index);

	public class ViaArrayList extends ArrayList<Integer> implements IntList {
		public void add(int value) {
			super.add(value);
		}

		public int getInt(int index) {
			return get(index);
		}
	}

	public class ViaJavaArray implements IntList {
		private int[] array = new int[8];
		private int size;

		public int size() {
			return size;
		}

		public void add(int value) {
			if (size >= array.length)
				array = Arrays.copyOf(array, array.length * 2);
			array[size++] = value;
		}

		public int getInt(int index) {
			if (index < 0 || index >= size)
				throw new IndexOutOfBoundsException();
			return array[index];
		}
	}
}
