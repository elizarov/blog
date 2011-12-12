import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

	/**
	 * Use direct byte buffer, default byte order.
	 */
	public class ViaByteBuffer1 implements IntList {
		private ByteBuffer buf = ByteBuffer.allocateDirect(32);
		private int size;

		public int size() {
			return size;
		}

		public void add(int value) {
			if (buf.position() >= buf.capacity()) {
				ByteBuffer larger = ByteBuffer.allocateDirect(buf.capacity() * 2);
				buf.rewind();
				larger.put(buf);
				buf = larger;
			}
			buf.putInt(value);
			size++;
		}

		public int getInt(int index) {
			if (index < 0 || index >= size)
				throw new IndexOutOfBoundsException();
			return buf.getInt(index * 4);
		}
	}

	/**
	 * Use direct by buffer, native byte order.
	 */
	public class ViaByteBuffer2 implements IntList {
		private ByteBuffer buf = allocate(32);
		private int size;

		private static ByteBuffer allocate(int size) {
			ByteBuffer buf = ByteBuffer.allocateDirect(size);
			buf.order(ByteOrder.nativeOrder());
			return buf;
		}

		public int size() {
			return size;
		}

		public void add(int value) {
			if (buf.position() >= buf.capacity()) {
				ByteBuffer larger = allocate(buf.capacity() * 2);
				buf.rewind();
				larger.put(buf);
				buf = larger;
			}
			buf.putInt(value);
			size++;
		}

		public int getInt(int index) {
			if (index < 0 || index >= size)
				throw new IndexOutOfBoundsException();
			return buf.getInt(index * 4);
		}
	}

	/**
	 * Use heap byte buffer, native byte order.
	 */
	public class ViaByteBuffer3 implements IntList {
		private ByteBuffer buf = allocate(32);
		private int size;

		private static ByteBuffer allocate(int size) {
			ByteBuffer buf = ByteBuffer.allocate(size);
			buf.order(ByteOrder.nativeOrder());
			return buf;
		}

		public int size() {
			return size;
		}

		public void add(int value) {
			if (buf.position() >= buf.capacity()) {
				ByteBuffer larger = allocate(buf.capacity() * 2);
				buf.rewind();
				larger.put(buf);
				buf = larger;
			}
			buf.putInt(value);
			size++;
		}

		public int getInt(int index) {
			if (index < 0 || index >= size)
				throw new IndexOutOfBoundsException();
			return buf.getInt(index * 4);
		}
	}
}
