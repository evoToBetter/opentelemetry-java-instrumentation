/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.results;

import io.opentelemetry.agents.Agent;
import io.opentelemetry.config.TestConfig;

public class AppPerfResults {

  private final Agent agent;
  private final TestConfig config;
  private final double iterationAvg;
  private final double iterationP95;
  private final double requestAvg;
  private final double requestP95;
  private final long totalGCTime;
  private final long totalAllocated;
  private final MinMax heapUsed;
  private final float maxThreadContextSwitchRate;
  private final long startupDurationMs;
  private final long peakThreadCount;

  private AppPerfResults(Builder builder) {
    this.agent = builder.agent;
    this.config = builder.config;
    this.iterationAvg = builder.iterationAvg;
    this.iterationP95 = builder.iterationP95;
    this.requestAvg = builder.requestAvg;
    this.requestP95 = builder.requestP95;
    this.totalGCTime = builder.totalGCTime;
    this.totalAllocated = builder.totalAllocated;
    this.heapUsed = builder.heapUsed;
    this.maxThreadContextSwitchRate = builder.maxThreadContextSwitchRate;
    this.startupDurationMs = builder.startupDurationMs;
    this.peakThreadCount = builder.peakThreadCount;
  }

  Agent getAgent() {
    return agent;
  }

  String getAgentName() {
    return agent.getName();
  }

  TestConfig getConfig() {
    return config;
  }

  double getIterationAvg() {
    return iterationAvg;
  }

  double getIterationP95() {
    return iterationP95;
  }

  double getRequestAvg() {
    return requestAvg;
  }

  double getRequestP95() {
    return requestP95;
  }

  long getTotalGCTime() {
    return totalGCTime;
  }

  long getTotalAllocated() {
    return totalAllocated;
  }

  double getTotalAllocatedMB() {
    return totalAllocated / (1024.0 * 1024.0);
  }

  MinMax getHeapUsed() {
    return heapUsed;
  }

  float getMaxThreadContextSwitchRate() {
    return maxThreadContextSwitchRate;
  }

  long getStartupDurationMs() {
    return startupDurationMs;
  }

  public long getPeakThreadCount() {
    return peakThreadCount;
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {
    private long startupDurationMs;
    private Agent agent;
    private TestConfig config;
    private double iterationAvg;
    private double iterationP95;
    private double requestAvg;
    private double requestP95;
    private long totalGCTime;
    private long totalAllocated;
    private MinMax heapUsed;
    private float maxThreadContextSwitchRate;
    private long peakThreadCount;

    AppPerfResults build() {
      return new AppPerfResults(this);
    }

    Builder agent(Agent agent) {
      this.agent = agent;
      return this;
    }

    Builder config(TestConfig config) {
      this.config = config;
      return this;
    }

    Builder iterationAvg(double iterationAvg) {
      this.iterationAvg = iterationAvg;
      return this;
    }

    Builder iterationP95(double iterationP95) {
      this.iterationP95 = iterationP95;
      return this;
    }

    Builder requestAvg(double requestAvg) {
      this.requestAvg = requestAvg;
      return this;
    }

    Builder requestP95(double requestP95) {
      this.requestP95 = requestP95;
      return this;
    }

    Builder totalGCTime(long totalGCTime) {
      this.totalGCTime = totalGCTime;
      return this;
    }

    Builder totalAllocated(long totalAllocated) {
      this.totalAllocated = totalAllocated;
      return this;
    }

    Builder heapUsed(MinMax heapUsed) {
      this.heapUsed = heapUsed;
      return this;
    }

    Builder maxThreadContextSwitchRate(float maxThreadContextSwitchRate) {
      this.maxThreadContextSwitchRate = maxThreadContextSwitchRate;
      return this;
    }

    Builder startupDurationMs(long startupDurationMs) {
      this.startupDurationMs = startupDurationMs;
      return this;
    }

    Builder peakThreadCount(long peakThreadCount) {
      this.peakThreadCount = peakThreadCount;
      return this;
    }
  }

  public static class MinMax {
    public final long min;
    public final long max;

    public MinMax(){
      this(Long.MAX_VALUE, Long.MIN_VALUE);
    }

    public MinMax(long min, long max){
      this.min = min;
      this.max = max;
    }

    public MinMax withMin(long min){
      return new MinMax(min, max);
    }

    public MinMax withMax(long max){
      return new MinMax(min, max);
    }
  }
}
