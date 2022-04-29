public class Tuple <T1, T2> {
    public T1 key;
    public T2 value;
    public Tuple(T1 a, T2 b) {
        this.key = a;
        this.value = b;
    }

    public String toString() {
        return "(" + key + ", " + value + ")";
    }
}
