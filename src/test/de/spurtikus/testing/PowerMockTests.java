package de.spurtikus.testing;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.InvocationHandler;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

/**
 * Mocking private and static methods.
 *
 * There are examples that replace methods with a "No Operation" and even with a different implementation.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PowerMockTests.InnerService.class, ExternalService.class})
public class PowerMockTests {

    // InnerService with private method
    public class InnerService {
        private void innerProcessing(String a, String b) {
            System.out.println("You should not see this line! innerProcessing " + a + " " + b);
        }
    }

    // OuterService calling innerService and ExternalService with static method
    private class OuterService {
        private InnerService innerService;

        public void setInnerService(InnerService innerService) {
            this.innerService = innerService;
        }


        public String processValues(String a, String b) {
            // call to innerService method
            innerService.innerProcessing(a, b);
            return "abc";
        }

        public int processStep() {
            // call to ExternalService static method
            return ExternalService.processStep(0);
        }
    }

    /**
     * This test shows how to replace/mock a void method call (InnerService.innerProcessing() ) with nothing.
     * This is done by using doNothing().when() construct.
     *
     * We use a mock here.
     *
     * @throws Exception on errors
     */
    @Test
    //@Ignore
    public void test_replace_void_method_with_doNothing() throws Exception {
        // Mock inner service
        InnerService mock = PowerMockito.mock(InnerService.class);

        // Set up outer service. Use mock object instead of real object to allow PowerMockito to
        // intercept method calls
        OuterService outerService = new OuterService();
        outerService.setInnerService(mock);

        // Replace innerProcessing() with doNothing(), i.e. method call
        // will have no effects at all
        doNothing().when(mock, "innerProcessing", anyString(), anyString());

        // Do the call
        String ret = outerService.processValues("xyz", "ggg");
        assertThat( ret, is("abc"));
    }

    /**
     * This test shows how to replace/mock a void method call (InnerService.innerProcessing() ) with nothing.
     * This is done by using doNothing().when() construct.
     *
     * We use a spy() instead of a mock() here.
     *
     * @throws Exception on errors
     */
    @Test
    public void test_replace_void_method_with_doNothing_and_spy() throws Exception {
        // Mock inner service by creating a spy object
        InnerService innerService = new InnerService();
        InnerService spy = PowerMockito.spy(innerService);

        // Set up outer service. Use spy object instead of real object to allow PowerMockito to
        // intercept method calls
        OuterService outerService = new OuterService();
        outerService.setInnerService(spy);

        // Replace innerProcessing() with doNothing(), i.e. method call
        // will have no effects at all
        doNothing().when(spy, "innerProcessing", anyString(), anyString());

        // Do the call
        String ret = outerService.processValues("xyz", "ggg");
        assertThat( ret, is("abc"));
    }

    /**
     * This test shows how to suppress a void method call (InnerService.innerProcessing()).
     * This is done by using org.powermock.api.support.membermodification.MemberModifier.suppress() construct.
     *
     * Used PowerMock methods:
     * import static org.powermock.api.support.membermodification.MemberModifier.suppress;
     * import static org.powermock.api.support.membermodification.MemberMatcher.method;
     *
     * More examples see here: https://blog.jayway.com/2013/03/05/beyond-mocking-with-powermock/

     * @throws Exception on errors
     */
    @Test
    public void test_replace_void_method_with_suppress() throws Exception {
        // Set up inner service
        InnerService innerService = new InnerService();

        // Set up outer service. Use mock object instead of real object to allow PowerMockito to
        // intercept method calls
        OuterService outerService = new OuterService();
        outerService.setInnerService(innerService);

        // Suppress innerProcessing() i.e. method call
        // will have no effects at all
        suppress(method(InnerService.class, "innerProcessing"));

        // Do the call
        String ret = outerService.processValues("xyz", "ggg");
        assertThat( ret, is("abc"));
    }


    /**
     * This test shows how to replace/mock a void method call (InnerService.innerProcessing() ) with another implementation.
     * This is done by using doAnswer().when() construct.
     *
     * @throws Exception on errors
     */
    @Test
    public void test_replace_void_method_with_doAnswer() throws Exception {
        // Mock inner service
        InnerService mock = PowerMockito.mock(InnerService.class);

        // Set up outer service.
        OuterService outerService = new OuterService();
        outerService.setInnerService(mock);

        // Replace innerProcessing() with a lambda expression using doAnswer(), i.e. method call
        // will execute the lambda expression instead of the original code.
        doAnswer(invocation -> {
            System.out.println("in method replacement");
            Object[] arguments = invocation.getArguments();
            if (arguments != null && arguments.length > 1 && arguments[0] != null && arguments[1] != null) {
                System.out.println("Replacement code called with arguments: " + Arrays.toString(arguments));
                assertEquals(arguments[0], "xyz");
            }
            return null;
        }).when(mock, "innerProcessing", anyString(), anyString());

        // Do the call
        String ret = outerService.processValues("xyz", "ggg");
    }

    /**
     * This test shows how to replace/mock a static method call (InnerService.innerProcessStep() ) with the return of
     * a fixed value.
     * This is done by using when().thenReturn construct.
     *
     * @throws Exception on errors
     */    @Test
    public void test_replace_static_method_with_thenReturn() throws Exception {
         // Mock some or all static methods on class ExternalService
        PowerMockito.mockStatic(ExternalService.class);

        // Set up outer service.
        OuterService outerService = new OuterService();

        // Replace static method processStep() with the return of a fixed value
        when(ExternalService.processStep(anyInt())).thenReturn(42);
        // line above is equal to: doReturn(42).when(ExternalService.processStep(anyInt()));

        // Do the call
        int retValue = outerService.processStep();
        assertThat(retValue, is(42));
    }

    /**
     * This test shows how to replace/mock a static method call (InnerService.innerProcessStep() ) with the return of
     * a fixed value.
     * This is done by using stub() construct.
     * Note that we do not need a "PowerMockito.mockStatic(ExternalService.class);" line when using stub().
     *
     * Used PowerMock methods:
     * import static org.powermock.api.support.membermodification.MemberModifier.stub;
     * import static org.powermock.api.support.membermodification.MemberMatcher.method;
     *
     * @throws Exception on errors
     */    @Test
    public void test_replace_static_method_with_stub() throws Exception {
        // Set up outer service.
        OuterService outerService = new OuterService();

        // Replace static method processStep() with the return of a fixed value
        stub(method(ExternalService.class, "processStep")).toReturn(42);

        // Do the call
        int retValue = outerService.processStep();
        assertThat(retValue, is(42));
    }


    /**
     * This test shows how to replace/mock a method call (ExternalService.processStep() ) with another implementation.
     * This is done by using replace(method().with()).when() construct.https://blog.jayway.com/2013/03/05/beyond-mocking-with-powermock/
     *
     * Used PowerMock methods:
     * import static org.powermock.api.support.membermodification.MemberMatcher.method;
     * import static org.powermock.api.support.membermodification.MemberMatcher.replace;
     * import java.lang.reflect.InvocationHandler
     *
     * Note that we do not need a "PowerMockito.mockStatic(ExternalService.class);" line when using stub().
     *
     * More examples see here: https://blog.jayway.com/2013/03/05/beyond-mocking-with-powermock/
     *
     * @throws Exception on errors
     */
    @Test
    public void test_replace_static_method_with_replace() throws Exception {
        // Set up outer service.
        OuterService outerService = new OuterService();

        // Replace static method processStep() with the return of a fixed value
         replace(method(ExternalService.class, "processStep")).with(
                new InvocationHandler() {
                    public Object invoke(Object object, Method method,
                                         Object[] arguments) throws Throwable {
                        System.out.println("Replacement code called with argument: " + arguments[0]);
                        return 42;
                        // Next lines would call the original code in most cases
                        //if (arguments[0].equals(0)) {
                        //    return 42;
                        //} else {
                        //    return method.invoke(object, arguments);
                        //}
                    }
                });

        // Do the call
        int retValue = outerService.processStep();
        assertThat(retValue, is(42));
    }

}