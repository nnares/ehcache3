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

package com.pany.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Alex Snaps
 */
@SuppressWarnings("serial")
public class Product implements Serializable {

  private final long id;
  private String mutable;

  public Product(final long key) {
    this.id = key;
  }

  public long getId() {
    return id;
  }

  public String getMutable() {
    return mutable;
  }

  public void setMutable(final String mutable) {
    this.mutable = mutable;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, mutable);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Product) {
      Product other = (Product) obj;
      return other.id == id && Objects.equals(mutable, other.mutable);
    } else {
      return false;
    }
  }
}
