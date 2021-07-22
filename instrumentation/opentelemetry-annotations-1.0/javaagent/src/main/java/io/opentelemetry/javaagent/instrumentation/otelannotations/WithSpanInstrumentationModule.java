/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.otelannotations;

import application.io.opentelemetry.extension.annotations.WithSpan;
import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import java.util.ArrayList;
import java.util.List;

/** Instrumentation for methods annotated with {@link WithSpan} annotation. */
@AutoService(InstrumentationModule.class)
public class WithSpanInstrumentationModule extends InstrumentationModule {

  public WithSpanInstrumentationModule() {
    super("opentelemetry-annotations");
  }

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    List<TypeInstrumentation> instrumentations=new ArrayList<>();
    instrumentations.add(new WithSpanInstrumentation());
    instrumentations.add(new WithSpanTestngInstrumentation());
    instrumentations.add(new SuiteRunnerWorkerInstrumentation());
    instrumentations.add(new SuiteRunnerInstrumentation());
    instrumentations.add(new TestMethodWorkerInstrumentation());
//    instrumentations.add(new TestRunnerInstrumentation());
    return instrumentations;
  }
}
