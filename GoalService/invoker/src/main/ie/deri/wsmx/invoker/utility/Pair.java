package ie.deri.wsmx.invoker.utility;

public class Pair <E, F> {

    private E e;
    private F f;
    
    public Pair (E theFirst, F theSecond){
        this.e = theFirst;
        this.f = theSecond;
    }
    
    public E getFirst(){
        return e;
    }
    
    public F getSecond(){
        return f;
    }
}
