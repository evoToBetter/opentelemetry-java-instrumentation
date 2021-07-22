package io.opentelemetry.javaagent.instrumentation.otelannotations;

import static io.opentelemetry.javaagent.instrumentation.api.Java8BytecodeBridge.currentContext;
import static io.opentelemetry.javaagent.instrumentation.otelannotations.WithSpanTracer.tracer;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.testng.ISuite;
import java.util.HashMap;
import java.util.Map;

public class WithSpanTestngInstrumentation implements TypeInstrumentation {

  private static final String listenerClassName="com.ericsson.commonlibrary.open.telemetry.listener.OpentelemetryTestngListener";

  private static final Map<String, Span> spans=new HashMap<>();

  private static final Map<String, Scope> scopes=new HashMap<>();

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named(listenerClassName);
  }

  @Override
  public void transform(TypeTransformer transformer) {
//    transformer.applyAdviceToMethod(
//        isMethod()
//        .and(named("onStart"))
//        .and(takesArguments(1))
//        .and(takesArgument(0,named("org.testng.ISuite")))
//        , WithSpanTestngInstrumentation.class.getName()+"$PerformSuiteOnStartAdvice");
    applyAdvice(transformer,"onStart1","PerformSuiteOnStartAdvice", "org.testng.ISuite");
//    applyAdvice(transformer,"onFinish","PerformSuiteOnFinishAdvice", "org.testng.ISuite");
//    applyAdvice(transformer,"onTestStart","PerformOnTestStartAdvice", "org.testng.ITestResult");
//
//    applyAdvice(transformer,"onTestSuccess","PerformOnTestSuccessAdvice", "org.testng.ITestResult");
//    applyAdvice(transformer,"onTestFailure","PerformOnTestFailureAdvice", "org.testng.ITestResult");
//    applyAdvice(transformer,"onTestSkipped","PerformOnTestSkippedAdvice", "org.testng.ITestResult");
//    applyAdvice(transformer,"onTestFailedButWithinSuccessPercentage","PerformOnTestFailedButWithinSuccessPercentageAdvice", "org.testng.ITestResult");
//
//    applyAdvice(transformer,"onStart","PerformOnTestClassStartAdvice", "org.testng.ITestContext");
//    applyAdvice(transformer,"onFinish","PerformOnTestClassFinishAdvice", "org.testng.ITestContext");
//
//    applyAdvice(transformer,"beforeInvocation","PerformBeforeInvocationAdvice", "org.testng.IInvokedMethod","org.testng.ITestResult","org.testng.ITestContext");
//    applyAdvice(transformer,"afterInvocation","PerformAfterInvocationAdvice", "org.testng.IInvokedMethod","org.testng.ITestResult","org.testng.ITestContext");
  }
  @SuppressWarnings({"unused","SystemOut"})
  private static void applyAdvice(TypeTransformer transformer, String methodName, String adviceClassName, String... parameterClassNames){
    System.out.println("apply advice "+ methodName);
    ElementMatcher.Junction matcher=isMethod().and(named(methodName));
    matcher=matcher.and(takesArguments(parameterClassNames.length));
    for(int i=0;i<parameterClassNames.length;i++){
      matcher=matcher.and(takesArgument(i,named(parameterClassNames[i])));
    }
    System.out.println("apply matcher: "+matcher.toString());
    transformer.applyAdviceToMethod(matcher,WithSpanTestngInstrumentation.class.getName()+"$"+ adviceClassName);
    System.out.println("Transformer : "+transformer);
  }
  @SuppressWarnings({"unused","SystemOut"})
  public static class PerformSuiteOnStartAdvice{

    @Advice.OnMethodEnter()
    public static void onEnter(
        @Advice.Argument(0) org.testng.ISuite iSuite,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope
    ){
      System.out.println("PerformSuiteOnStartAdvice");
      String name=iSuite.getName();
      String key=name + "_suite";
      context=tracer().startSpan(currentContext(), key, SpanKind.INTERNAL);
      scope=context.makeCurrent();
//      spans.put(key,Span.fromContext(context));
//      scopes.put(key,context.makeCurrent());
    }
    @Advice.OnMethodExit()
    public static void onExit(
        @Advice.Argument(0)ISuite iSuite,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope
    ){
      System.out.println("PerformSuiteOnStartAdvice finish");
//      String name=iSuite.getName();
//      String key=name + "_suite";
//      Scope scope=scopes.get(key);
//      scope.close();
//      spans.get(key).end();
      scope.close();
      tracer().end(context);

    }
  }
//  @SuppressWarnings({"unused","SystemOut"})
//  public static class PerformSuiteOnFinishAdvice{
//    @Advice.OnMethodEnter(suppress = Throwable.class)
//    public static void onEnter(
//        @Advice.Argument(0)ISuite iSuite
//    ){
//      System.out.println("PerformSuiteOnFinishAdvice");
//      String name=iSuite.getName();
//      String key=name + "_suite";
//      scopes.get(key).close();
//      spans.get(key).end();
//    }
//  }
//  @SuppressWarnings({"unused","SystemOut"})
//  public static class PerformOnTestStartAdvice{
//    @Advice.OnMethodEnter(suppress = Throwable.class)
//    public static void onEnter(
//        @Advice.Argument(0) ITestResult iTestResult
//    ){
//      System.out.println("PerformOnTestStartAdvice");
//      String name=iTestResult.getName()==null?iTestResult.getName():iTestResult.getTestName();
//      String key=name + "_test";
//      Context context=tracer().startSpan(currentContext(), name, SpanKind.INTERNAL);
//      spans.put(key,Span.fromContext(context));
//      scopes.put(key,context.makeCurrent());
//    }
//  }
//  @SuppressWarnings({"unused","SystemOut"})
//  public static class PerformOnTestSuccessAdvice{
//    @Advice.OnMethodEnter(suppress = Throwable.class)
//    public static void onEnter(
//        @Advice.Argument(0) ITestResult iTestResult
//    ){
//      System.out.println("PerformOnTestSuccessAdvice");
//      String name=iTestResult.getName()==null?iTestResult.getName():iTestResult.getTestName();
//      String key=name + "_test";
//      scopes.get(key).close();
//      Span span=spans.get(key);
//      span.setAttribute("result",iTestResult.getStatus());
//      span.end();
//    }
//  }
//  @SuppressWarnings({"unused","SystemOut"})
//  public static class PerformOnTestFailureAdvice{
//    @Advice.OnMethodEnter(suppress = Throwable.class)
//    public static void onEnter(
//        @Advice.Argument(0) ITestResult iTestResult
//    ){
//      System.out.println("PerformOnTestFailureAdvice");
//      String name=iTestResult.getName()==null?iTestResult.getName():iTestResult.getTestName();
//      String key=name + "_test";
//      scopes.get(key).close();
//      Span span=spans.get(key);
//      span.setAttribute("result",iTestResult.getStatus());
//      span.end();
//    }
//  }
//  @SuppressWarnings({"unused","SystemOut"})
//  public static class PerformOnTestSkippedAdvice{
//    @Advice.OnMethodEnter(suppress = Throwable.class)
//    public static void onEnter(
//        @Advice.Argument(0) ITestResult iTestResult
//    ){
//      System.out.println("PerformOnTestSkippedAdvice");
//      String name=iTestResult.getName()==null?iTestResult.getName():iTestResult.getTestName();
//      String key=name + "_test";
//      scopes.get(key).close();
//      Span span=spans.get(key);
//      span.setAttribute("result",iTestResult.getStatus());
//      span.end();
//    }
//  }
//  @SuppressWarnings({"unused","SystemOut"})
//  public static class PerformOnTestFailedButWithinSuccessPercentageAdvice{
//    @Advice.OnMethodEnter(suppress = Throwable.class)
//    public static void onEnter(
//        @Advice.Argument(0) ITestResult iTestResult
//    ){
//      System.out.println("PerformOnTestFailedButWithinSuccessPercentageAdvice");
//      String name=iTestResult.getName()==null?iTestResult.getName():iTestResult.getTestName();
//      String key=name + "_test";
//      scopes.get(key).close();
//      Span span=spans.get(key);
//      span.setAttribute("result",iTestResult.getStatus());
//      span.end();
//    }
//  }
//  @SuppressWarnings({"unused","SystemOut"})
//  public static class PerformOnTestClassStartAdvice{
//    @Advice.OnMethodEnter(suppress = Throwable.class)
//    public static void onEnter(
//        @Advice.Argument(0) ITestContext iTestContext
//    ){
//      System.out.println("PerformOnTestClassStartAdvice");
//      String name=iTestContext.getName();
//      String key=name + "_test";
//      Context context=tracer().startSpan(currentContext(), name, SpanKind.INTERNAL);
//      spans.put(key,Span.fromContext(context));
//      scopes.put(key,context.makeCurrent());
//    }
//  }
//  @SuppressWarnings({"unused","SystemOut"})
//  public static class PerformOnTestClassFinishAdvice{
//    @Advice.OnMethodEnter(suppress = Throwable.class)
//    public static void onEnter(
//        @Advice.Argument(0) ITestContext iTestContext
//    ){
//      System.out.println("PerformOnTestClassFinishAdvice");
//      String name=iTestContext.getName();
//      String key=name + "_test";
//
//      scopes.get(key).close();
//      spans.get(key).end();
//    }
//  }
//  @SuppressWarnings({"unused","SystemOut"})
//  public static class PerformBeforeInvocationAdvice{
//    @Advice.OnMethodEnter(suppress = Throwable.class)
//    public static void onEnter(
//        @Advice.Argument(0) IInvokedMethod iInvokedMethod
//    ){
//      System.out.println("PerformBeforeInvocationAdvice");
//      String name=iInvokedMethod.getTestMethod().getMethodName();
//      String key=name + "_test";
//      Context context=tracer().startSpan(currentContext(), name, SpanKind.INTERNAL);
//
//      spans.put(key,Span.fromContext(context));
//      scopes.put(key,context.makeCurrent());
//    }
//  }
//  @SuppressWarnings({"unused","SystemOut"})
//  public static class PerformAfterInvocationAdvice{
//    @Advice.OnMethodEnter(suppress = Throwable.class)
//    public static void onEnter(
//        @Advice.Argument(0) IInvokedMethod iInvokedMethod
//    ){
//
//      System.out.println("PerformAfterInvocationAdvice");
//      String name=iInvokedMethod.getTestMethod().getMethodName();
//      String key=name + "_test";
//      scopes.get(key).close();
//      spans.get(key).end();
//    }
//  }


}
