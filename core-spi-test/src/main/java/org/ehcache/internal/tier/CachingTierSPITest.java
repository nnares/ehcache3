/*
 * Copyright Terracotta, Inc.
 * Copyright IBM Corp. 2024, 2025
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ehcache.internal.tier;

import org.junit.Test;

/**
 * @author Aurelien Broszniowski
 */
public abstract class CachingTierSPITest<K, V> {

  protected abstract CachingTierFactory<K, V> getCachingTierFactory();

  @Test
  public void testGetOrComputeIfAbsent() throws Exception {
    CachingTierGetOrComputeIfAbsent<K, V> testSuite = new CachingTierGetOrComputeIfAbsent<>(getCachingTierFactory());
    testSuite.runTestSuite().reportAndThrow();
  }

  @Test
  public void testCachingTierRemove() throws Exception {
    CachingTierRemove<K, V> testSuite = new CachingTierRemove<>(getCachingTierFactory());
    testSuite.runTestSuite().reportAndThrow();
  }

  @Test
  public void testCachingTierClear() throws Exception {
    CachingTierClear<K, V> testSuite = new CachingTierClear<>(getCachingTierFactory());
    testSuite.runTestSuite().reportAndThrow();
  }

  @Test
  public void testCachingTierInvalidate() throws Exception {
    CachingTierInvalidate<K, V> testSuite = new CachingTierInvalidate<>(getCachingTierFactory());
    testSuite.runTestSuite().reportAndThrow();
  }

}
