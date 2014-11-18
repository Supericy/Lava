package kosiec.lava;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chad on 11/13/2014.
 */
public class Container {

	public static final Object SKIP_PARAM = new Object();

	public static final String DEFAULT_NAME = "~default";

	private final Map<Dependency, Factory<?>> classes;

	public Container()
	{
		classes = new HashMap<Dependency, Factory<?>>();
	}

	public <T> Dependency register(Class<T> cls)
	{
		return register(cls, cls);
	}

	public <T> Dependency register(Class<T> cls, Class<? extends T> implementation)
	{
		return register(cls, new DefaultFactory<T>(implementation));
	}

	public <T> Dependency register(Class<T> cls, Factory<T> factory)
	{
		Dependency key = new Dependency(cls, DEFAULT_NAME);
		classes.put(key, factory);
		return key;
	}



	public <T> Dependency singleton(Class<T> cls)
	{
		return singleton(cls, cls);
	}

	public <T> Dependency singleton(Class<T> cls, T instance)
	{
		return singleton(cls, new SingletonFactory<T>(instance));
	}

	public <T> Dependency singleton(Class<T> cls, Class<? extends T> implementation)
	{
		return singleton(cls, new DefaultFactory<T>(implementation));
	}

	public <T> Dependency singleton(Class<T> cls, Factory<T> factory)
	{
		return register(cls, new SingletonFactory<T>(factory));
	}



	public <T> T resolve(Class<? extends T> cls)
	{
		return resolve(cls, DEFAULT_NAME);
	}

	// factory should be safe to cast, no need for the warning.
	@SuppressWarnings(value="unchecked")
	public <T> T resolve(Class<T> cls, String name)
	{
		Factory<?> factory = classes.get(new Dependency(cls, name));

		if (factory == null)
			throw new ContainerException("couldn't resolve class for " + cls);

		return (T) factory.make(this);
	}

	/**
	 * ====================
	 * = INTERNAL CLASSES =
	 * ====================
	 */

	public class Dependency {
		private Class<?> cls;
		private String name;

		public Dependency(Class<?> cls, String name)
		{
			this.cls = cls;
			this.name = name;
		}

		public final Dependency named(String name)
		{
			if (name == null)
				throw new IllegalArgumentException("can't set the name to null");

			/**
			 * Since we're updating the key in the hashmap, we need to remove ourselves from the map and then add
			 * ourselves again after we've updated the name.
			 */

			// store factory temporarily
			Factory<?> factory = classes.get(this);

			// remove ourselves from the class map
			classes.remove(this);

			// update our name
			this.name = name;

			// put ourselves back into the class map
			classes.put(this, factory);

			return this;
		}

//		public final void with(Object ... params)
//		{
//			Factory<?> factory = classes.get(this);

//			factory.
//		}

		@Override
		public final int hashCode()
		{
			return (cls.toString() + "#" + name).hashCode();
		}

		@Override
		public final boolean equals(Object obj)
		{
			if (!(obj instanceof Dependency))
				return false;

			Dependency other = (Dependency) obj;

			return this.cls.equals(other.cls) &&
					(
							(this.name == null && other.name == null) ||
							(this.name != null && this.name.equals(other.name)) ||
							(other.name != null && other.name.equals(this.name))
					);
		}
	}

	private static class SingletonFactory<T> implements Factory<T> {
		private final Factory<T> factory;
		private T instance = null;

		public SingletonFactory(Factory<T> factory)
		{
			if (factory == null)
				throw new IllegalArgumentException("Can't have a null factory for a singleton");

			this.factory = factory;
		}

		public SingletonFactory(T instance)
		{
			this.factory = null;
			this.instance = instance;
		}

		@Override
		public T make(Container container)
		{
			if (instance == null)
			{
				// check is redundant since we guarantee that either factory != null, or instance != null
				if (factory == null)
					throw new NullPointerException("Can't create an instance without a factory");

				instance = factory.make(container);
			}


			return instance;
		}
	}

	private static class DefaultFactory<T> implements Factory<T> {

		private final Class<? extends T> implementation;
		private Object[] params;

		public DefaultFactory(Class<? extends T> implementation)
		{
			this.implementation = implementation;
			this.params = new Object[0];
		}

//		public void setParameters(Object ... params)
//		{
//			if (params == null)
//				throw new NullPointerException("DefaultFactory::setParameters parameters can't be null");
//
//			this.params = params;
//		}
//
//		public Object getParameter(int i)
//		{
//			return i < params.length ? params[i] : SKIP_PARAM;
//		}

		@Override
		public T make(Container container)
		{
			Object instance = null;

			Constructor<?>[] constructors = implementation.getConstructors();

			// TODO refactor, currently only looks at the first constructor
			for (Constructor<?> constructor : constructors)
			{
				Class<?>[] parameters = constructor.getParameterTypes();
				Object[] args = new Object[parameters.length];
				Object param;

				// resolve all parameters
				for (int i = 0; i < parameters.length; i++)
				{
//					param = getParameter(i);

//					args[i] = param != SKIP_PARAM ? param : container.resolve(parameters[i]);

//					if ((param = getParameter(i)) != SKIP)
					args[i] = container.resolve(parameters[i]);
				}

				// attempt to create an instance
				try
				{
					instance = constructor.newInstance(args);

					// created an instance, so we can break from constructors
					break;
				}
				catch (InvocationTargetException e)
				{
					throw new ContainerException(e.getMessage(), e);
				}
				catch (InstantiationException e)
				{
					throw new ContainerException(e.getMessage(), e);
				}
				catch (IllegalAccessException e)
				{
					throw new ContainerException(e.getMessage(), e);
				}
			}

			return (T) instance;
		}
	}

}
