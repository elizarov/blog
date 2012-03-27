/**
 * @author Roman Elizarov
 */
public enum IntOp {
	ID {
		@Override
		public int compute(int x) {
			return x;
		}
	},

	X2 {
		@Override
		public int compute(int x) {
			return x * 2;
		}
	},

	X27 {
		@Override
		public int compute(int x) {
			return x * 27;
		}
	},

	X31 {
		@Override
		public int compute(int x) {
			return x * 31;
		}
	},

	X37 {
		@Override
		public int compute(int x) {
			return x * 37;
		}
	};

	public abstract int compute(int x);
}
