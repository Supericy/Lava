package kosiec.lava.lava;

/**
 * Created by Chad on 11/13/2014.
 */
public interface Factory<T> {

	public T make(Container container);

}
