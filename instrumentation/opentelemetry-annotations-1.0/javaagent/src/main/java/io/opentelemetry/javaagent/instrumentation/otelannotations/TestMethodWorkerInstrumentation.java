package io.opentelemetry.javaagent.instrumentation.otelannotations;

import static io.opentelemetry.javaagent.instrumentation.api.Java8BytecodeBridge.currentContext;
import static io.opentelemetry.javaagent.instrumentation.otelannotations.WithSpanTracer.tracer;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

public class TestMethodWorkerInstrumentation implements TypeInstrumentation {
  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("org.testng.internal.TestMethodWorker");
  }
  @SuppressWarnings({"unused","SystemOut"})
  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        isMethod()
            .and(named("invokeTestMethods"))
            .and(takesArguments(3))
            .and(takesArgument(0,named("org.testng.ITestNGMethod")))
        .and(takesArgument(1,named("java.lang.Object")))
        .and(takesArgument(2,named("org.testng.ITestContext")))
        , TestMethodWorkerInstrumentation.class.getName()+"$PerformInvokeTestMethods"
    );
    System.out.println("Apply PerformInvokeTestMethods");
  }
  @SuppressWarnings({"unused","SystemOut"})
  public static class PerformInvokeTestMethods{
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onEnter(
        @Advice.Argument(0) ITestNGMethod method,
        @Advice.Argument(1) Object object,
        @Advice.Argument(2) ITestContext testContext,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope
    ){
      System.out.println("PerformInvokeTestMethods");
      String name=method.getMethodName();
      context=tracer().startSpan(currentContext(), name, SpanKind.INTERNAL);
      scope=context.makeCurrent();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void onExit(
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope,
        @Advice.Thrown Throwable throwable
    ){
      System.out.println("PerformInvokeTestMethods finish");
      if(scope==null){
        System.out.println("There is no scope in PerformRunSuite finish");
        return;
      }
      scope.close();
      if(throwable!=null){
        tracer().endExceptionally(context,throwable);
      }else{
        tracer().end(context);
      }
    }
  }

}
