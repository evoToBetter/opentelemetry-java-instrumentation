package io.opentelemetry.javaagent.instrumentation.otelannotations;

import static io.opentelemetry.javaagent.instrumentation.api.Java8BytecodeBridge.currentContext;
import static io.opentelemetry.javaagent.instrumentation.api.Java8BytecodeBridge.currentSpan;
import static io.opentelemetry.javaagent.instrumentation.otelannotations.WithSpanTracer.tracer;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Series;

public class TestNGInstrumentation implements TypeInstrumentation {
  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("org.testng.TestNG");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        isMethod()
            .and(named("run")),
        this.getClass().getName()+"$PerformRun"
    );
  }

  public static class PerformRun {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onEnter(
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope,
        @Advice.Local("startTime") Instant start
    ){
//      System.out.println("PerformRunSuite");
      context=tracer().startSpan(currentContext(), "TestNG", SpanKind.INTERNAL);
      scope=context.makeCurrent();
      start= Instant.now();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void onExit(
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope,
        @Advice.Local("startTime") Instant start,
        @Advice.Thrown Throwable throwable
    ){
//      System.out.println("PerformRunSuite finish");
      if(scope==null){
//        System.out.println("There is no scope in PerformRunSuite finish");
        return;
      }
//      StringBuilder grafanaLink=new StringBuilder();
//      grafanaLink.append("http://localhost:3000/d/GHxpRvimz/prometheus-jvm-overview-arms?orgId=1&from=1627460700000&to=1627462200000");
      Instant end=Instant.now();
      long grafanaScopeStart=start.minus(10, ChronoUnit.SECONDS).toEpochMilli();
      long grafanaScopeEnd=end.plus(10,ChronoUnit.SECONDS).toEpochMilli();

      String grafanaUrl=System.getProperty("grafana_url");
      String dashboardPath=System.getProperty("dashboard_path");
      String grafanaUser=System.getProperty("grafana_user");
      String grafanaPass=System.getProperty("grafana_password");
      String dashboardId=System.getProperty("dashboaard_id");
      String grafanaLink=String.format("%s/%s?orgId=1&from=%d&to=%d",grafanaUrl,dashboardPath,grafanaScopeStart,grafanaScopeEnd);
      currentSpan().setAttribute("grafanaLink", grafanaLink);
      Client client=new Client(Protocol.HTTP);
      Request request=new Request(Method.POST,grafanaUrl+"/api/annotations");
      ChallengeResponse challengeResponse=new ChallengeResponse(ChallengeScheme.HTTP_BASIC, grafanaUser,grafanaPass);
      request.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,new Series<>(Header.class));
      request.setChallengeResponse(challengeResponse);
      request.getHeaders().add(HeaderConstants.HEADER_CONTENT_TYPE,"application/json");
      request.getClientInfo().accept(MediaType.APPLICATION_JSON);
      Representation representation=new StringRepresentation("{\n"
          + "  \"dashboardId\":"+dashboardId+",\n"
          + "  \"time\":"+start.toEpochMilli()+",\n"
          + "  \"timeEnd\":"+end.toEpochMilli()+",\n"
          + "  \"tags\":[\""+currentSpan().getSpanContext().getTraceId()+"\"],\n"
          + "  \"text\":\"traceID\"\n"
          + "}",MediaType.APPLICATION_JSON);
      request.setEntity(representation);
      Response response=client.handle(request);
      scope.close();
      if(throwable!=null){
        tracer().endExceptionally(context,throwable);
      }else{
        tracer().end(context);
      }
    }
  }
}
