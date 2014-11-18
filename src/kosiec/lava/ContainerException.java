package kosiec.lava;

/**
 * Created by Chad on 11/13/2014.
 */
public class ContainerException extends RuntimeException {

	private Throwable throwable;

	public ContainerException(String message)
	{
		super(message, null);
	}

	public ContainerException(String message, Throwable throwable)
	{
		super(message);

		this.throwable = throwable;
	}

	public Throwable getThrowable()
	{
		return throwable;
	}

}
