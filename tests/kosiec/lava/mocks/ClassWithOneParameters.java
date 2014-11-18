package kosiec.lava.mocks;

/**
 * Created by Chad on 11/13/2014.
 */
public class ClassWithOneParameters {

	private final GenericClassOne g1;

	public ClassWithOneParameters(GenericClassOne g1)
	{
		this.g1 = g1;
	}

	public GenericClassOne getFirstParameter()
	{
		return g1;
	}

}
