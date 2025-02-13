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

package org.ehcache.jsr107;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;
import javax.cache.spi.CachingProvider;

import static java.util.Collections.singleton;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * LoaderWriterConfigTest
 */
@RunWith(MockitoJUnitRunner.class)
public class LoaderWriterConfigTest {

  @Mock
  private CacheLoader<Long, String> cacheLoader;
  @Mock
  private CacheWriter<Long, String> cacheWriter;
  private CachingProvider cachingProvider;

  @Before
  public void setUp() {
    cachingProvider = Caching.getCachingProvider();
  }

  @After
  public void tearDown() {
    cachingProvider.close();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void enablingWriteThroughDoesNotForceReadThrough() throws Exception {
    MutableConfiguration<Long, String> config = getConfiguration(false, cacheLoader, true, cacheWriter);

    Cache<Long, String> cache = cachingProvider.getCacheManager().createCache("writingCache", config);
    cache.put(42L, "Tadam!!!");
    Set<Long> keys = singleton(25L);
    cache.loadAll(keys, false, null);

    cache.get(100L);

    verify(cacheLoader).loadAll(keys);
    verifyNoMoreInteractions(cacheLoader);
    verify(cacheWriter).write(any(Cache.Entry.class));
  }

  @Test
  public void enablingReadThroughDoesNotForceWriteThrough() throws Exception {
    MutableConfiguration<Long, String> config = getConfiguration(true, cacheLoader, false, cacheWriter);

    Cache<Long, String> cache = cachingProvider.getCacheManager().createCache("writingCache", config);
    cache.put(42L, "Tadam!!!");

    cache.get(100L);

    verifyNoInteractions(cacheWriter);
    verify(cacheLoader).load(100L);
  }

  private MutableConfiguration<Long, String> getConfiguration(final boolean readThrough, final CacheLoader<Long, String> cacheLoader,
                                                              final boolean writeThrough, final CacheWriter<Long, String> cacheWriter) {
    MutableConfiguration<Long, String> config = new MutableConfiguration<>();
    config.setStoreByValue(false);
    config.setTypes(Long.class, String.class);
    config.setReadThrough(readThrough);
    config.setCacheLoaderFactory(() -> cacheLoader);
    config.setWriteThrough(writeThrough);
    config.setCacheWriterFactory(() -> cacheWriter);
    return config;
  }
}
