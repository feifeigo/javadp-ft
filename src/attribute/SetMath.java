package attribute;

import java.util.LinkedList;
import java.util.List;

public class SetMath {
    public <T> List<T> intersect(LinkedList<T> ls, LinkedList<T> ls2) {
        List<T> list = new LinkedList<T>(ls);
        list.retainAll(ls2);
        return list;
    }

    public <T> List<T> union(LinkedList<T> ls, LinkedList<T> ls2) {
        List<T> list = new LinkedList<T>(ls);
        List<T> list2 = new LinkedList<T>(ls2);
        list2.removeAll(ls);
        list.addAll(ls2);
        return list;
    }

    public <T> List<T> diff(LinkedList<T> ls, LinkedList<T> ls2) {
        List<T> list = new LinkedList<T>(ls);
        list.removeAll(ls2);
        return list;
    }
}
