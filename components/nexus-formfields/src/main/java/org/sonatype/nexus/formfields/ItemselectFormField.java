/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.formfields;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * An item-selection {@link FormField}.
 *
 * @since 3.1
 */
public class ItemselectFormField
    extends AbstractFormField<String>
    implements Selectable
{
  public static final String TYPE = "itemselect";

  private String storeApi;

  private final Map<String, String> storeFilters = new HashMap<>();

  private String idMapping;

  private String nameMapping;

  public ItemselectFormField(final String id,
                             final String label,
                             final String helpText,
                             final boolean required,
                             final String initialValue)
  {
    super(id, label, helpText, required, null, initialValue);
  }

  public ItemselectFormField(final String id,
                             final String label,
                             final String helpText,
                             final boolean required)
  {
    this(id, label, helpText, required, null);
  }

  public ItemselectFormField(final String id,
                             final String label,
                             final String helpText)
  {
    this(id, label, helpText, OPTIONAL);
  }

  public ItemselectFormField(final String id,
                             final String label)
  {
    this(id, label, null);
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public String getStoreApi() {
    return storeApi;
  }

  @Override
  @Nullable
  public Map<String, String> getStoreFilters() {
    return storeFilters.isEmpty() ? null : storeFilters;
  }

  @Override
  public String getIdMapping() {
    return idMapping;
  }

  @Override
  public String getNameMapping() {
    return nameMapping;
  }

  public void setStoreApi(final String storeApi) {
    this.storeApi = storeApi;
  }

  public void setIdMapping(final String idMapping) {
    this.idMapping = idMapping;
  }

  public void setNameMapping(final String nameMapping) {
    this.nameMapping = nameMapping;
  }

  public void addStoreFilter(final String property, final String value) {
    storeFilters.put(property, value);
  }

  public void setButtons(final String... buttons) {
    getAttributes().put("buttons", buttons);
  }

  public void setFromTitle(final String title) {
    getAttributes().put("fromTitle", title);
  }

  public void setToTitle(final String title) {
    getAttributes().put("toTitle", title);
  }
}
