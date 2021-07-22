package io.opentelemetry.javaagent.instrumentation.otelannotations;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.HashMap;
import java.util.Map;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener2;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class WithSpanListener implements ISuiteListener, ITestListener, IInvokedMethodListener2 {
  private final Map<String, Span> tracingSpans=new HashMap<>();
  private final Map<String, Scope> tracingScopes=new HashMap<>();
  private WithSpanTracer tracer;

  @Override
  public void onStart(ISuite iSuite) {
    this.tracer = WithSpanTracer.tracer();
    String name=iSuite.getName();
    String key=name + "_suite";
    Span span = Span.fromContext(tracer.startSpan(name));
    Scope scope=span.makeCurrent();
    span.setAttribute("output_dir", iSuite.getOutputDirectory());
    tracingSpans.put(key, span);
    tracingScopes.put(key,scope);
  }
  @Override
  public void onStart(ITestContext iTestContext) {
    String name=iTestContext.getName();
    String key=name+"_test_execution";
    Span span = Span.fromContext(this.tracer.startSpan(name));
    Scope scop=span.makeCurrent();
    tracingScopes.put(key,scop);
    tracingSpans.put(key, span);
  }

  @Override
  public void onFinish(ITestContext iTestContext) {
    String name=iTestContext.getName();
    String key=name+"_test_execution";
    tracingScopes.get(key).close();
    tracingSpans.get(key).end();
  }

  @Override
  public void onFinish(ISuite iSuite) {
    String key=iSuite.getName()+"_suite";
    tracingScopes.get(key).close();
    tracingSpans.get(iSuite.getName() + "_suite").end();
  }

  @Override
  public void onTestStart(ITestResult iTestResult) {
    String name=iTestResult.getTestName()==null?iTestResult.getName():iTestResult.getTestName();
    String key=name+"_test";
    Span span = Span.fromContext(this.tracer.startSpan(name));
    Scope scop=span.makeCurrent();
    tracingSpans.put(key, span);
    tracingScopes.put(key, scop);
  }

  @Override
  public void onTestSuccess(ITestResult iTestResult) {
    String name=iTestResult.getTestName()==null?iTestResult.getName():iTestResult.getTestName();
    String key=name+"_test";
    tracingScopes.get(key).close();
    Span span=tracingSpans.get(key);
    span.setAttribute("result","success");
    span.end();
  }

  @Override
  public void onTestFailure(ITestResult iTestResult) {
    String name=iTestResult.getTestName()==null?iTestResult.getName():iTestResult.getTestName();
    String key=name+"_test";
    tracingScopes.get(key).close();
    Span span=tracingSpans.get(key);
    span.setAttribute("result","failure");
    span.end();
  }

  @Override
  public void onTestSkipped(ITestResult iTestResult) {
    String name=iTestResult.getTestName()==null?iTestResult.getName():iTestResult.getTestName();
    String key=name+"_test";
    tracingScopes.get(key).close();
    Span span=tracingSpans.get(key);
    span.setAttribute("result","skipped");
    span.end();
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
    String name=iTestResult.getTestName()==null?iTestResult.getName():iTestResult.getTestName();
    String key=name+"_test";
    tracingScopes.get(key).close();
    Span span=tracingSpans.get(key);
    span.setAttribute("result","failedButWithinSuccessPercentage");
    span.end();
  }



  @Override
  public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult, ITestContext iTestContext) {
    String name=iInvokedMethod.getTestMethod().getMethodName();
    String key=name + "_method";
    Span span = Span.fromContext(this.tracer.startSpan(name));
    tracingSpans.put(key, span);
  }
  @Override
  public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {

  }
  @Override
  public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult, ITestContext iTestContext) {
    String name=iInvokedMethod.getTestMethod().getMethodName();
    String key=name + "_method";
    tracingSpans.get(key).end();
  }
  @Override
  public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {

  }



}
