import java.util.*;

public class OrderedDict<T1, T2> extends LinkedHashMap<T1, T2> {
    public int indexOf(T1 key) {
        int i = 0;
        for (T1 t : keySet()) {
            if (t.equals(key))
                return i;
            i++;
        }
        return -1;
    }

    public OrderedDict(List<Tuple<T1, T2>> x) {
        for (Tuple<T1, T2> e : x) {
            put(e.key, e.value);
        }
    }

    @SafeVarargs
    public OrderedDict(Tuple<T1, T2>... x) {
        for (Tuple<T1, T2> e : x) {
            put(e.key, e.value);
        }
    }
}
