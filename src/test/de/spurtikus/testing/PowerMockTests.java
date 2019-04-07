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

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PowerMockTests.InnerService.class, ExternalService.class})
public class PowerMockTests {

    public class InnerService {
        private void innerProcessing(String a, String b) {
            System.out.println("You should not see this line! innerProcessing " + a + " " + b);
        }
    }

    private class OuterService {
        private InnerService innerService;

        public void setInnerService(InnerService innerService) {
            this.innerService = innerService;
        }

        public String processValues(String a, String b) {
            innerService.innerProcessing(a, b);
            return "abc";
        }

        public int processStep() {
            return ExternalService.processStep(0);
        }
    }

    /**
     * This test shows how to replace/mock a void method call (InnerService.innerProcessing() ) with nothing.
     * This is done by using doNothing().when() construct.
     * @throws Exception on errors
     */
    @Test
    //@Ignore
    public void test_doNothing_replacement_for_void_method() throws Exception {
        // Set up inner service
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
     * This test shows how to replace/mock a void method call (InnerService.innerProcessing() ) with another implementation.
     * This is done by using doAnswer().when() construct.
     *
     * @throws Exception on errors
     */
    @Test
    public void test_answer_replacement_for_void_method() throws Exception {
        InnerService innerService = new InnerService();
        InnerService spy = PowerMockito.spy(innerService);

        // Set up outer service.
        OuterService outerService = new OuterService();
        outerService.setInnerService(spy);

        // Replace innerProcessing() with a lambda expression using doAnswer(), i.e. method call
        // will execute the lambda expression instead of the original code.
        doAnswer(invocation -> {
            System.out.println("in method replacement");
            Object[] arguments = invocation.getArguments();
            if (arguments != null && arguments.length > 1 && arguments[0] != null && arguments[1] != null) {
                System.out.println("method called with arguments: " + Arrays.toString(arguments));
                assertEquals(arguments[0], "xyz");
            }
            return null;
        }).when(spy, "innerProcessing", anyString(), anyString());

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
    public void test_replace_static_method() throws Exception {
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

    /* The replacement does not work for unknown reason :-( */
    /*@Test
    public void test_replace_static_method_x() throws Exception {
        // Mock some or all static methods on class ExternalService
        PowerMockito.mockStatic(ExternalService.class);

        // Set up outer service.
        OuterService outerService = new OuterService();

        // Replace static method processStep() with the return of a fixed value
         replace(method(ExternalService.class, "processStep")).with(
                new InvocationHandler() {
                    public Object invoke(Object object, Method method,
                                         Object[] arguments) throws Throwable {
                        System.out.println("p=" + arguments[0]);
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
    }*/

}