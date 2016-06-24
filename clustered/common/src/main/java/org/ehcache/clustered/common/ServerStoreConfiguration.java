/*
 * Copyright Terracotta, Inc.
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

package org.ehcache.clustered.common;

import java.io.Serializable;

/**
 * Configuration properties for a {@code ServerStore} instance.
 */
public class ServerStoreConfiguration implements Serializable {
  private static final long serialVersionUID = 5452646838836730816L;

  /**
   * The server-side resource allocation parameters.
   */
  private final PoolAllocation poolAllocation;

  private final String storedKeyType;
  private final String storedValueType;
  private final String actualKeyType;
  private final String actualValueType;
  private final String keySerializerType;
  private final String valueSerializerType;
  private final Consistency consistency;
  // TODO: Loader/Writer configuration ...

  public ServerStoreConfiguration(PoolAllocation poolAllocation,
                                  String storedKeyType,
                                  String storedValueType,
                                  String actualKeyType,
                                  String actualValueType,
                                  String keySerializerType,
                                  String valueSerializerType,
                                  Consistency consistency) {
    this.poolAllocation = poolAllocation;
    this.storedKeyType = storedKeyType;
    this.storedValueType = storedValueType;
    this.actualKeyType = actualKeyType;
    this.actualValueType = actualValueType;
    this.keySerializerType = keySerializerType;
    this.valueSerializerType = valueSerializerType;
    this.consistency = consistency;
  }

  public PoolAllocation getPoolAllocation() {
    return poolAllocation;
  }

  public String getStoredKeyType() {
    return storedKeyType;
  }

  public String getStoredValueType() {
    return storedValueType;
  }

  public String getActualKeyType() {
    return actualKeyType;
  }

  public String getActualValueType() {
    return actualValueType;
  }

  public String getKeySerializerType() {
    return keySerializerType;
  }

  public String getValueSerializerType() {
    return valueSerializerType;
  }

  public Consistency getConsistency() {
    return consistency;
  }

  public boolean isCompatible(ServerStoreConfiguration otherConfiguration, StringBuilder sb) {

    boolean isCompatible;
    PoolAllocation otherPoolAllocation = otherConfiguration.getPoolAllocation();

    isCompatible = comparePoolAllocationType(sb, otherPoolAllocation);
    if(isCompatible) {
      if( !(otherPoolAllocation instanceof PoolAllocation.Unknown) ) {
        if (poolAllocation instanceof PoolAllocation.Dedicated) {
          PoolAllocation.Dedicated serverDedicatedAllocation = (PoolAllocation.Dedicated)poolAllocation;
          PoolAllocation.Dedicated clientDedicatedAllocation = (PoolAllocation.Dedicated)otherPoolAllocation;
          if (compareField(sb, "resourcePoolDedicatedResourceName",
              serverDedicatedAllocation.getResourceName(),
              clientDedicatedAllocation.getResourceName())) {
            if (clientDedicatedAllocation.getSize() != serverDedicatedAllocation.getSize()) {
              appendFault(sb, "resourcePoolDedicatedSize", serverDedicatedAllocation.getSize(), clientDedicatedAllocation.getSize());
              isCompatible &= false;
            }
          } else {
            isCompatible &= false;
          }
        } else if (poolAllocation instanceof PoolAllocation.Shared) {
          isCompatible &= compareField(sb, "resourcePoolSharedPoolName",
              ((PoolAllocation.Shared)poolAllocation).getResourcePoolName(),
              ((PoolAllocation.Shared)otherPoolAllocation).getResourcePoolName());
        }
      }
    }
    isCompatible &= compareField(sb, "storedKeyType", storedKeyType, otherConfiguration.getStoredKeyType());
    isCompatible &= compareField(sb, "storedValueType", storedValueType, otherConfiguration.getStoredValueType());
    isCompatible &= compareField(sb, "actualKeyType", actualKeyType, otherConfiguration.getActualKeyType());
    isCompatible &= compareField(sb, "actualValueType", actualValueType, otherConfiguration.getActualValueType());
    isCompatible &= compareField(sb, "keySerializerType", keySerializerType, otherConfiguration.getKeySerializerType());
    isCompatible &= compareField(sb, "valueSerializerType", valueSerializerType, otherConfiguration.getValueSerializerType());
    isCompatible &= compareConsistencyField(sb, consistency, otherConfiguration.getConsistency());

    return isCompatible;
  }

    private boolean comparePoolAllocationType(StringBuilder sb, PoolAllocation clientPoolAllocation) {

    if (clientPoolAllocation instanceof PoolAllocation.Unknown || poolAllocation.getClass().getName().equals(clientPoolAllocation.getClass().getName())) {
      return true;
    }

    appendFault(sb, "resourcePoolType", getClassName(poolAllocation), getClassName(clientPoolAllocation));
    return false;
  }

    private String getClassName(Object obj) {
    if(obj != null) {
      return obj.getClass().getName();
    } else {
      return null;
    }
  }

    private boolean compareConsistencyField(StringBuilder sb, Consistency serverConsistencyValue, Consistency clientConsistencyValue) {
    if((serverConsistencyValue == null && clientConsistencyValue == null)
        || (serverConsistencyValue != null && serverConsistencyValue.equals(clientConsistencyValue))) {
      return true;
    }

    appendFault(sb, "consistencyType", serverConsistencyValue, clientConsistencyValue);
    return false;
  }

  private boolean compareField(StringBuilder sb, String fieldName, String serverConfigValue, String clientConfigValue) {
    if ((serverConfigValue == null && clientConfigValue == null)
        || (serverConfigValue != null && serverConfigValue.equals(clientConfigValue))) {
      return true;
    }

    appendFault(sb, fieldName, serverConfigValue, clientConfigValue);
    return false;
  }

  private void appendFault(StringBuilder sb, String fieldName, Object serverConfigValue, Object clientConfigValue) {
    sb.append("\n\t").append(fieldName)
        .append(" existing: ").append(serverConfigValue)
        .append(", desired: ").append(clientConfigValue);
  }

  public interface PoolAllocation extends Serializable {

    /**
     * Describes a dedicated-size allocation for clustered storage.  When using a dedicated allocation,
     * storage is allocated from the server-based resource specified.
     */
    final static class Dedicated implements PoolAllocation {
      private static final long serialVersionUID = -2249181124582282204L;
      private final long size;
      private final String resourceName;

      /**
       * Create a new dedicated {@code PoolAllocation}.
       *
       * @param resourceName the server-side resource from a dedicated-size allocation is made; if {@code null},
       *                     the allocation is made from the default resource
       * @param size the size, in bytes, of the allocation
       */
      public Dedicated(String resourceName, long size) {
        this.resourceName = resourceName;
        this.size = size;
      }

      /**
       * Gets the size, in bytes, for the dedicated allocation to make from the server-side storage resource for
       * a store configured with this {@code PoolAllocation}.
       *
       * @return the dedicated allocation size
       */
      public long getSize() {
        return size;
      }

      /**
       * Gets the name of the server-side storage resource from which allocations for a store configured
       * with this {@code PoolAllocation} are made.
       *
       * @return the server-side resource name
       */
      public String getResourceName() {
        return resourceName;
      }
    }

    /**
     * Describes a shared allocation for clustered storage.  When using a shared pool,
     * allocation requests are satisfied from the server-based shared resource pool identified.
     */
    final static class Shared implements PoolAllocation {
      private static final long serialVersionUID = -5111316473831788364L;
      private final String resourcePoolName;

      /**
       * Create a new shared {@code PoolAllocation}.
       *
       * @param resourcePoolName the server-side shared resource pool from which allocations are made
       */
      public Shared(String resourcePoolName) {
        this.resourcePoolName = resourcePoolName;
      }

      /**
       * Gets the name of the server-side storage resource pool from which allocations for a store configured
       * with this {@code PoolAllocation} are sub-allocated.
       *
       * @return the server-side resource pool name
       */
      public String getResourcePoolName() {
        return resourcePoolName;
      }
    }

    /**
     * Creates a Pool Allocation which inherits the Shared or Dedicated Pool Allocation from a cache which is already configured on the server.
     */
    final static class Unknown implements PoolAllocation {
      private static final long serialVersionUID = 3584540926973176260L;
    }
  }

}
