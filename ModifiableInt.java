/**
 * @author Roman Elizarov
 */
public class ModifiableInt {
    private int value;

    public ModifiableInt(int value) {
        this.value = value;
    }

    public ModifiableInt(ModifiableInt other) {
        this.value = other.value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
