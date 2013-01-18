/**
 * @author Roman Elizarov
 */
public class ModifiableIntContainer {
    private ModifiableInt c;

    public ModifiableIntContainer(ModifiableInt c) {
        this.c = new ModifiableInt(c);
    }

    public ModifiableInt getC() {
        return new ModifiableInt(c);
    }

    public void setC(ModifiableInt c) {
        this.c = new ModifiableInt(c);
    }
}
