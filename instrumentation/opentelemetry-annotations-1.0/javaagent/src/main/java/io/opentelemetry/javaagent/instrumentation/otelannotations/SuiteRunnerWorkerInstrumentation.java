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
import org.testng.internal.SuiteRunnerMap;
import org.testng.xml.XmlSuite;

public class SuiteRunnerWorkerInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("org.testng.SuiteRunnerWorker");
  }
  @SuppressWarnings({"unused","SystemOut"})
  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        isMethod()
        .and(named("runSuite"))
        .and(takesArguments(2))
        .and(takesArgument(0,named("org.testng.internal.SuiteRunnerMap")))
        .and(takesArgument(1,named("org.testng.xml.XmlSuite")))
        , SuiteRunnerWorkerInstrumentation.class.getName()+"$PerformRunSuite"
    );
    System.out.println("Apply PerformRunSuite");
  }
  @SuppressWarnings({"unused","SystemOut"})
  public static class PerformRunSuite{

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onEnter(
        @Advice.Argument(0) SuiteRunnerMap suiteRunnerMap,
        @Advice.Argument(1) XmlSuite xmlSuite,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope
    ){
      System.out.println("PerformRunSuite");
      String name=xmlSuite.getName();
      context=tracer().startSpan(currentContext(), name, SpanKind.INTERNAL);
      scope=context.makeCurrent();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void onExit(
        @Advice.Argument(0) SuiteRunnerMap suiteRunnerMap,
        @Advice.Argument(1) XmlSuite xmlSuite,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope,
        @Advice.Thrown Throwable throwable
    ){
      System.out.println("PerformRunSuite finish");
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
