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
import java.util.HashMap;
import java.util.Map;

import org.ehcache.Status;
import org.ehcache.core.spi.store.Store;
import org.ehcache.core.store.SimpleTestStore;
import org.ehcache.spi.resilience.StoreAccessException;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
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
public class EhcacheBasicContainsKeyTest extends EhcacheBasicCrudBase {

  /**
   * Tests {@link Ehcache#containsKey(Object) Ehcache.containsKey} with a {@code null} key.
   */
  @Test
  public void testContainsKeyNull() throws Exception {
    final SimpleTestStore realStore = new SimpleTestStore(Collections.<String, String>emptyMap());
    this.store = spy(realStore);
    final Ehcache<String, String> ehcache = this.getEhcache();

    try {
      ehcache.containsKey(null);
      fail();
    } catch (NullPointerException e) {
      // Expected
    }
    verifyNoInteractions(this.resilienceStrategy);
  }

  /**
   * Tests {@link Ehcache#containsKey(Object) Ehcache.containsKey} over an empty cache.
   */
  @Test
  public void testContainsKeyEmpty() throws Exception {
    final SimpleTestStore realStore = new SimpleTestStore(Collections.<String, String>emptyMap());
    this.store = spy(realStore);
    final Ehcache<String, String> ehcache = this.getEhcache();

    assertFalse(ehcache.containsKey("key"));
    verifyNoInteractions(this.resilienceStrategy);
  }

  /**
   * Tests {@link Ehcache#containsKey(Object) Ehcache.containsKey} over an empty cache
   * where {@link Store#containsKey(Object) Store.containsKey} throws a
   * {@link StoreAccessException StoreAccessException}.
   */
  @Test
  public void testContainsKeyEmptyStoreAccessException() throws Exception {
    final SimpleTestStore realStore = new SimpleTestStore(Collections.<String, String>emptyMap());
    this.store = spy(realStore);
    doThrow(new StoreAccessException("")).when(this.store).containsKey("key");
    final Ehcache<String, String> ehcache = this.getEhcache();

    ehcache.containsKey("key");
    verify(this.resilienceStrategy).containsKeyFailure(eq("key"), any(StoreAccessException.class));
  }

  /**
   * Tests {@link Ehcache#containsKey(Object) Ehcache.containsKey} over a cache holding
   * the target key.
   */
  @Test
  public void testContainsKeyContains() throws Exception {
    final SimpleTestStore realStore = new SimpleTestStore(this.getTestStoreEntries());
    this.store = spy(realStore);
    final Ehcache<String, String> ehcache = this.getEhcache();

    assertTrue(ehcache.containsKey("keyA"));
    verifyNoInteractions(this.resilienceStrategy);
  }

  /**
   * Tests {@link Ehcache#containsKey(Object) Ehcache.containsKey} over a cache holding
   * the target key where {@link Store#containsKey(Object) Store.containsKey}
   * throws a {@link StoreAccessException StoreAccessException}.
   */
  @Test
  public void testContainsKeyContainsStoreAccessException() throws Exception {
    final SimpleTestStore realStore = new SimpleTestStore(this.getTestStoreEntries());
    this.store = spy(realStore);
    doThrow(new StoreAccessException("")).when(this.store).containsKey("keyA");
    final Ehcache<String, String> ehcache = this.getEhcache();

    ehcache.containsKey("keyA");
    verify(this.resilienceStrategy).containsKeyFailure(eq("keyA"), any(StoreAccessException.class));
  }

  /**
   * Tests {@link Ehcache#containsKey(Object) Ehcache.containsKey} over a non-empty cache
   * not holding the target key.
   */
  @Test
  public void testContainsKeyMissing() throws Exception {
    final SimpleTestStore realStore = new SimpleTestStore(this.getTestStoreEntries());
    this.store = spy(realStore);
    final Ehcache<String, String> ehcache = this.getEhcache();

    assertFalse(ehcache.containsKey("missingKey"));
    verifyNoInteractions(this.resilienceStrategy);
  }

  /**
   * Tests {@link Ehcache#containsKey(Object) Ehcache.containsKey} over a non-empty cache
   * not holding the target key where {@link Store#containsKey(Object) Store.containsKey}
   * throws a {@link StoreAccessException StoreAccessException}.
   */
  @Test
  public void testContainsKeyMissingStoreAccessException() throws Exception {
    final SimpleTestStore realStore = new SimpleTestStore(this.getTestStoreEntries());
    this.store = spy(realStore);
    doThrow(new StoreAccessException("")).when(this.store).containsKey("missingKey");
    final Ehcache<String, String> ehcache = this.getEhcache();

    ehcache.containsKey("missingKey");
    verify(this.resilienceStrategy).containsKeyFailure(eq("missingKey"), any(StoreAccessException.class));
  }

  private Map<String, String> getTestStoreEntries() {
    final Map<String, String> storeEntries = new HashMap<>();
    storeEntries.put("key1", "value1");
    storeEntries.put("keyA", "valueA");
    storeEntries.put("key2", "value2");
    storeEntries.put("keyB", "valueB");
    return storeEntries;
  }

  /**
   * Gets an initialized {@link Ehcache Ehcache}.
   *
   * @return a new {@code Ehcache} instance
   */
  private Ehcache<String, String> getEhcache()
      throws Exception {
    final Ehcache<String, String> ehcache =
      new Ehcache<>(CACHE_CONFIGURATION, this.store, resilienceStrategy, cacheEventDispatcher);
    ehcache.init();
    assertThat("cache not initialized", ehcache.getStatus(), Matchers.is(Status.AVAILABLE));
    return ehcache;
  }
}
