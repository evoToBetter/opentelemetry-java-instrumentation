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
import org.testng.TestRunner;

public class SuiteRunnerInstrumentation implements TypeInstrumentation {
  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("org.testng.SuiteRunner");
  }
  @SuppressWarnings({"SystemOut"})
  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
            isMethod()
            .and(named("runTest"))
            .and(takesArguments(1))
            .and(takesArgument(0,named("org.testng.TestRunner")))
        , this.getClass().getName()+"$PerformRunTest"
    );
//    System.out.println("Apply PerformRunTest");
  }
  @SuppressWarnings({"unused","SystemOut"})
  public static class PerformRunTest{
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onEnter(
        @Advice.Argument(0) TestRunner runner,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope
    ){
//      System.out.println("PerformRunTest");
      String name=runner.getCurrentXmlTest().getName();
//      String name ="new_name";
      context=tracer().startSpan(currentContext(), name, SpanKind.INTERNAL);
      scope=context.makeCurrent();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void onExit(
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope,
        @Advice.Thrown Throwable throwable
    ){
//      System.out.println("PerformRunTest finish");
      if(scope==null){
//        System.out.println("There is no scope in PerformRunSuite finish");
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
