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

package org.ehcache.jsr107.internal;

import org.ehcache.jsr107.config.ConfigurationElementState;
import org.ehcache.jsr107.config.Jsr107Configuration;
import org.ehcache.xml.CacheManagerServiceConfigurationParser;
import org.ehcache.jsr107.Jsr107Service;
import org.ehcache.xml.exceptions.XmlConfigurationException;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;

import static java.lang.Boolean.parseBoolean;

/**
 * @author Alex Snaps
 */
@Component
public class Jsr107ServiceConfigurationParser extends Jsr107Parser<Jsr107Configuration> implements CacheManagerServiceConfigurationParser<Jsr107Service, Jsr107Configuration> {

  private static final String ENABLE_MANAGEMENT_ALL_ATTRIBUTE = "enable-management";
  private static final String JSR_107_COMPLIANT_ATOMICS_ATTRIBUTE = "jsr-107-compliant-atomics";
  private static final String ENABLE_STATISTICS_ALL_ATTRIBUTE = "enable-statistics";
  private static final String DEFAULT_TEMPLATE_ATTRIBUTE = "default-template";
  private static final String CACHE_NAME_ATTRIBUTE = "name";
  private static final String TEMPLATE_NAME_ATTRIBUTE = "template";

  @Override
  public Jsr107Configuration parse(final Element fragment, ClassLoader classLoader) {
    boolean jsr107CompliantAtomics = true;
    ConfigurationElementState enableManagementAll = ConfigurationElementState.UNSPECIFIED;
    ConfigurationElementState enableStatisticsAll = ConfigurationElementState.UNSPECIFIED;
    if (fragment.hasAttribute(JSR_107_COMPLIANT_ATOMICS_ATTRIBUTE)) {
      jsr107CompliantAtomics = parseBoolean(fragment.getAttribute(JSR_107_COMPLIANT_ATOMICS_ATTRIBUTE));
    }
    if (fragment.hasAttribute(ENABLE_MANAGEMENT_ALL_ATTRIBUTE)) {
      enableManagementAll = parseBoolean(fragment.getAttribute(ENABLE_MANAGEMENT_ALL_ATTRIBUTE)) ? ConfigurationElementState.ENABLED : ConfigurationElementState.DISABLED;
    }
    if (fragment.hasAttribute(ENABLE_STATISTICS_ALL_ATTRIBUTE)) {
      enableStatisticsAll = parseBoolean(fragment.getAttribute(ENABLE_STATISTICS_ALL_ATTRIBUTE)) ? ConfigurationElementState.ENABLED : ConfigurationElementState.DISABLED;
    }
    final String defaultTemplate = fragment.getAttribute(DEFAULT_TEMPLATE_ATTRIBUTE);
    final HashMap<String, String> templates = new HashMap<>();
    final NodeList childNodes = fragment.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      final Node node = childNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        final Element item = (Element)node;
        templates.put(item.getAttribute(CACHE_NAME_ATTRIBUTE), item.getAttribute(TEMPLATE_NAME_ATTRIBUTE));
      }
    }

    return new Jsr107Configuration(defaultTemplate, templates, jsr107CompliantAtomics, enableManagementAll, enableStatisticsAll);
  }

  @Override
  public Class<Jsr107Service> getServiceType() {
    return Jsr107Service.class;
  }

  @Override
  public Element safeUnparse(Document target, Jsr107Configuration serviceCreationConfiguration) {
    throw new XmlConfigurationException("XML translation of JSR-107 cache elements are not supported");
  }

}
