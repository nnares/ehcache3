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

package com.pany.ehcache;

import org.ehcache.expiry.ExpiryPolicy;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * @author Alex Snaps
 */
public class MyExpiry implements ExpiryPolicy<Object, Object> {
  @Override
  public Duration getExpiryForCreation(Object key, Object value) {
    return Duration.ofSeconds(42);
  }

  @Override
  public Duration getExpiryForAccess(Object key, Supplier<? extends Object> value) {
    return Duration.ofSeconds(42);
  }

  @Override
  public Duration getExpiryForUpdate(Object key, Supplier<? extends Object> oldValue, Object newValue) {
    return Duration.ofSeconds(42);
  }
}
