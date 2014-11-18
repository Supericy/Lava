package kosiec.lava;

import kosiec.lava.mocks.*;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

public class ContainerTest {

	private Container container;

	@Before
	public void setup()
	{
		container = new Container();
	}

	@Test
	public void testResolvingClassWithNoParameters() throws Exception
	{
		container.register(GenericMockClass.class, GenericMockClass.class);
		container.register(GenericClassOne.class, GenericClassOne.class);
		container.register(GenericClassTwo.class, GenericClassTwo.class);

		assertThat(container.resolve(GenericMockClass.class), instanceOf(GenericMockClass.class));
		assertThat(container.resolve(GenericClassOne.class), instanceOf(GenericClassOne.class));
		assertThat(container.resolve(GenericClassTwo.class), instanceOf(GenericClassTwo.class));
	}

	@Test
	public void testResolvingClassWithOneParameter() throws Exception
	{
		container.register(GenericClassOne.class, GenericClassOne.class);
		container.register(ClassWithOneParameters.class, ClassWithOneParameters.class);

		assertThat(container.resolve(ClassWithOneParameters.class), instanceOf(ClassWithOneParameters.class));
	}

	@Test
	public void testResolvingClassWithTwoParameters() throws Exception
	{
		container.register(GenericClassOne.class, GenericClassOne.class);
		container.register(GenericClassTwo.class, GenericClassTwo.class);

		container.register(ClassWithTwoParameters.class, ClassWithTwoParameters.class);

		assertThat(container.resolve(ClassWithTwoParameters.class), instanceOf(ClassWithTwoParameters.class));
	}

	@Test
	public void testResolvingClassFromInterface() throws Exception
	{
		container.register(MockInterface.class, MockInterfaceImplementation.class);

		assertThat(container.resolve(MockInterface.class), instanceOf(MockInterfaceImplementation.class));
	}

	@Test
	public void testResolvingClassWithInterfaceDependency() throws Exception
	{
		container.register(MockInterface.class, MockInterfaceImplementation.class);

		container.register(ClassWithInterfaceDependency.class, ClassWithInterfaceDependency.class);

		assertThat(container.resolve(ClassWithInterfaceDependency.class), instanceOf(ClassWithInterfaceDependency.class));
	}

	@Test
	public void testResolvingNamedClass() throws Exception
	{
		container.register(MockInterface.class, MockImplementation1.class).named("impl1");
		container.register(MockInterface.class, MockImplementation2.class).named("impl2");

		assertThat(container.resolve(MockInterface.class, "impl1"), instanceOf(MockImplementation1.class));
		assertThat(container.resolve(MockInterface.class, "impl2"), instanceOf(MockImplementation2.class));
	}

	@Test
	public void testResolvingFromFactory() throws Exception
	{
		container.register(MockInterface.class, new Factory<MockInterface>()
		{
			@Override
			public MockInterface make(Container container)
			{
				return new MockImplementation1();
			}
		});

		assertThat(container.resolve(MockInterface.class), instanceOf(MockImplementation1.class));
	}

	@Test
	public void testResolvingSingleParametersFromFactory() throws Exception
	{
		container.register(GenericClassOne.class, new Factory<GenericClassOne>()
		{
			@Override
			public GenericClassOne make(Container container)
			{
				return new GenericClassOne();
			}
		});

		container.register(ClassWithOneParameters.class, ClassWithOneParameters.class);

		assertThat(container.resolve(ClassWithOneParameters.class), instanceOf(ClassWithOneParameters.class));
	}

	@Test
	public void testResolvingClassThatIsItsOwnImplementation() throws Exception
	{
		container.register(GenericClassOne.class);

		assertThat(container.resolve(GenericClassOne.class), instanceOf(GenericClassOne.class));
	}

	@Test
	public void testResolvingNamedClassWithSameImplementation() throws Exception
	{
//		container.register(GenericClassOne.class, "class-1");
//		container.register(GenericClassOne.class, "class-2");
	}

	@Test(expected = ContainerException.class)
	public void testResolvingClassThatIsNotRegistered() throws Exception
	{
		container.resolve(GenericClassOne.class);
	}

	@Test
	public void testResolvingSingleton() throws Exception
	{
		container.singleton(GenericClassOne.class);

		assertThat(container.resolve(GenericClassOne.class), instanceOf(GenericClassOne.class));

		// make sure we are only creating one instance
		GenericClassOne ref_1 = container.resolve(GenericClassOne.class);
		GenericClassOne ref_2 = container.resolve(GenericClassOne.class);

		assertThat(ref_1, sameInstance(ref_2));
	}

	@Test
	public void testRegisteringAndResolvingInstance() throws Exception
	{
		GenericClassOne ref_1 = new GenericClassOne();

		container.singleton(GenericClassOne.class, ref_1);

		assertThat(container.resolve(GenericClassOne.class), sameInstance(ref_1));
	}

//	@Test
//	public void testRegisteringClassWithVariables() throws Exception
//	{
//		GenericClassOne g1 = new GenericClassOne();
//		container.register(ClassWithOneParameters.class)
//				 .with(g1);
//
//		assertThat(container.resolve(ClassWithOneParameters.class).getFirstParameter(), sameInstance(g1));
//	}

}