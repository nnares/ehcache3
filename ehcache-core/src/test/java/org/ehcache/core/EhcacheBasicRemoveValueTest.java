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
package org.ehcache.core;

import java.util.Collections;
import java.util.EnumSet;

import org.ehcache.Status;
import org.ehcache.core.statistics.CacheOperationOutcomes;
import org.ehcache.core.store.SimpleTestStore;
import org.ehcache.spi.resilience.StoreAccessException;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author Abhilash
 *
 */
public class EhcacheBasicRemoveValueTest extends EhcacheBasicCrudBase {

  @Test
  public void testRemoveNullNull() {
    Ehcache<String, String> ehcache = this.getEhcache();

    try {
      ehcache.remove(null, null);
      fail();
    } catch (NullPointerException e) {
      // expected
    }
  }

  @Test
  public void testRemoveKeyNull() throws Exception {
    Ehcache<String, String> ehcache = this.getEhcache();

    try {
      ehcache.remove("key", null);
      fail();
    } catch (NullPointerException e) {
      // expected
    }
  }

  @Test
  public void testRemoveNullValue() throws Exception {
    Ehcache<String, String> ehcache = this.getEhcache();

    try {
      ehcache.remove(null, "value");
      fail();
    } catch (NullPointerException e) {
      // expected
    }
  }

  /**
   * Tests the effect of a {@link Ehcache#remove(Object, Object)} for
   * <ul>
   *   <li>key not present in {@code Store}</li>
   * </ul>
   */
  @Test
  public void testRemoveValueNoStoreEntry() throws Exception {
    SimpleTestStore fakeStore = new SimpleTestStore(Collections.<String, String>emptyMap());
    this.store = spy(fakeStore);

    Ehcache<String, String> ehcache = this.getEhcache();

    assertFalse(ehcache.remove("key", "value"));
    verify(this.store).remove(eq("key"), eq("value"));
    verifyNoInteractions(this.resilienceStrategy);
    assertThat(fakeStore.getEntryMap().containsKey("key"), is(false));
    validateStats(ehcache, EnumSet.of(CacheOperationOutcomes.ConditionalRemoveOutcome.FAILURE_KEY_MISSING));
  }

  /**
   * Tests the effect of a {@link Ehcache#remove(Object, Object)} for
   * <ul>
   *   <li>key with unequal value in {@code Store}</li>
   * </ul>
   */
  @Test
  public void testRemoveValueUnequalStoreEntry() throws Exception {
    SimpleTestStore fakeStore = new SimpleTestStore(Collections.singletonMap("key", "unequalValue"));
    this.store = spy(fakeStore);

    Ehcache<String, String> ehcache = this.getEhcache();

    assertFalse(ehcache.remove("key", "value"));
    verify(this.store).remove(eq("key"), eq("value"));
    verifyNoInteractions(this.resilienceStrategy);
    assertThat(fakeStore.getEntryMap().get("key"), is(equalTo("unequalValue")));
    validateStats(ehcache, EnumSet.of(CacheOperationOutcomes.ConditionalRemoveOutcome.FAILURE_KEY_PRESENT));
  }

  /**
   * Tests the effect of a {@link Ehcache#remove(Object, Object)} for
   * <ul>
   *   <li>key with equal value in {@code Store}</li>
   * </ul>
   */
  @Test
  public void testRemoveValueEqualStoreEntry() throws Exception {
    SimpleTestStore fakeStore = new SimpleTestStore(Collections.singletonMap("key", "value"));
    this.store = spy(fakeStore);

    Ehcache<String, String> ehcache = this.getEhcache();

    assertTrue(ehcache.remove("key", "value"));
    verify(this.store).remove(eq("key"), eq("value"));
    verifyNoInteractions(this.resilienceStrategy);
    assertThat(fakeStore.getEntryMap().containsKey("key"), is(false));
    validateStats(ehcache, EnumSet.of(CacheOperationOutcomes.ConditionalRemoveOutcome.SUCCESS));
  }

  /**
   * Tests the effect of a {@link Ehcache#remove(Object, Object)} for
   * <ul>
   *   <li>key not present in {@code Store}</li>
   *   <li>>{@code Store.remove} throws</li>
   * </ul>
   */
  @Test
  public void testRemoveValueNoStoreEntryStoreAccessException() throws Exception {
    SimpleTestStore fakeStore = new SimpleTestStore(Collections.emptyMap());
    this.store = spy(fakeStore);
    doThrow(new StoreAccessException("")).when(this.store).remove(eq("key"), eq("value"));

    Ehcache<String, String> ehcache = this.getEhcache();

    ehcache.remove("key", "value");
    verify(this.store).remove(eq("key"), eq("value"));
    verify(this.resilienceStrategy).removeFailure(eq("key"), eq("value"), any(StoreAccessException.class));
    validateStats(ehcache, EnumSet.of(CacheOperationOutcomes.ConditionalRemoveOutcome.FAILURE));
  }

  /**
   * Tests the effect of a {@link Ehcache#remove(Object, Object)} for
   * <ul>
   *   <li>key with unequal value present in {@code Store}</li>
   *   <li>>{@code Store.remove} throws</li>
   * </ul>
   */
  @Test
  public void testRemoveValueUnequalStoreEntryStoreAccessException() throws Exception {
    SimpleTestStore fakeStore = new SimpleTestStore(Collections.singletonMap("key", "unequalValue"));
    this.store = spy(fakeStore);
    doThrow(new StoreAccessException("")).when(this.store).remove(eq("key"), eq("value"));

    Ehcache<String, String> ehcache = this.getEhcache();

    ehcache.remove("key", "value");
    verify(this.store).remove(eq("key"), eq("value"));
    verify(this.resilienceStrategy).removeFailure(eq("key"), eq("value"), any(StoreAccessException.class));
    validateStats(ehcache, EnumSet.of(CacheOperationOutcomes.ConditionalRemoveOutcome.FAILURE));
  }

  /**
   * Tests the effect of a {@link Ehcache#remove(Object, Object)} for
   * <ul>
   *   <li>key with equal value present in {@code Store}</li>
   *   <li>>{@code Store.remove} throws</li>
   * </ul>
   */
  @Test
  public void testRemoveValueEqualStoreEntryStoreAccessException() throws Exception {
    SimpleTestStore fakeStore = new SimpleTestStore(Collections.singletonMap("key", "value"));
    this.store = spy(fakeStore);
    doThrow(new StoreAccessException("")).when(this.store).remove(eq("key"), eq("value"));

    Ehcache<String, String> ehcache = this.getEhcache();

    ehcache.remove("key", "value");
    verify(this.store).remove(eq("key"), eq("value"));
    verify(this.resilienceStrategy).removeFailure(eq("key"), eq("value"), any(StoreAccessException.class));
    validateStats(ehcache, EnumSet.of(CacheOperationOutcomes.ConditionalRemoveOutcome.FAILURE));
  }

  /**
   * Gets an initialized {@link Ehcache Ehcache} instance
   *
   * @return a new {@code Ehcache} instance
   */
  private Ehcache<String, String> getEhcache() {
    Ehcache<String, String> ehcache = new Ehcache<>(CACHE_CONFIGURATION, this.store, resilienceStrategy, cacheEventDispatcher);
    ehcache.init();
    assertThat("cache not initialized", ehcache.getStatus(), is(Status.AVAILABLE));
    return ehcache;
  }
}
