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
package org.sonatype.nexus.repository.storage;

import java.util.HashMap;

import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.entity.EntityHelper;
import org.sonatype.nexus.orient.testsupport.DatabaseInstanceRule;

import com.google.common.collect.Iterables;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sonatype.nexus.common.entity.EntityHelper.id;
import static org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter.P_ATTRIBUTES;

public class AssetEntityAdapterTest
{
  @Rule
  public DatabaseInstanceRule database = DatabaseInstanceRule.inMemory("test");

  private ComponentEntityAdapter componentEntityAdapter;

  private AssetEntityAdapter assetEntityAdapter;

  private Bucket bucket;

  @Before
  public void setUp() {
    BucketEntityAdapter bucketEntityAdapter = new BucketEntityAdapter();
    componentEntityAdapter = new ComponentEntityAdapter(bucketEntityAdapter);
    assetEntityAdapter = new AssetEntityAdapter(bucketEntityAdapter, componentEntityAdapter);
    try (ODatabaseDocumentTx db = database.getInstance().connect()) {
      bucketEntityAdapter.register(db);
      componentEntityAdapter.register(db);
      assetEntityAdapter.register(db);
      bucket = new Bucket();
      bucket.attributes(new NestedAttributesMap(P_ATTRIBUTES, new HashMap<>()));
      bucket.setRepositoryName("test-repo");
      bucketEntityAdapter.addEntity(db, bucket);
    }
  }

  @Test
  public void testModifyAssetAfterBrowsingByComponent() {

    try (ODatabaseDocumentTx db = database.getInstance().acquire()) {
      db.begin();

      Component component = createComponent("some-group", "some-component", "1.0");
      componentEntityAdapter.addEntity(db, component);

      Asset asset = createAsset("some-asset", component);
      assetEntityAdapter.addEntity(db, asset);

      db.commit();
    }

    Asset asset;
    try (ODatabaseDocumentTx db = database.getInstance().acquire()) {
      db.begin();

      Component component = Iterables.getFirst(componentEntityAdapter.browseByQuery(
          db, "name = :name", singletonMap("name", "some-component"), singletonList(bucket), null), null);

      asset = Iterables.getFirst(assetEntityAdapter.browseByComponent(db, component), null);
      asset.attributes().child("mandatory").set("test-key", "test-value");
      assetEntityAdapter.editEntity(db, asset);

      db.commit();
    }

    assertThat(asset, is(notNullValue()));

    /*
     * Check the asset's attributes can be modified outside of the transaction.
     *
     * This used to fail because the attributes were backed by an OTrackedMap
     * which expected to find a live DB context whenever the map was mutated.
     * We now wrap this tracking map so it detaches when this isn't the case.
     *
     * It also wasn't obvious when you were modifying attributes. For example
     * just accessing a non-existent child section implicitly added it to the
     * map, causing it to mutate...
     */

    // mandatory section already exists, so this won't mutate the attributes
    assertTrue(asset.attributes().child("mandatory").contains("test-key"));

    // optional section doesn't exist yet, so this will mutate the attributes
    assertFalse(asset.attributes().child("optional").contains("test-key"));
  }

  private Component createComponent(final String group, final String name, final String version) {
    Component component = new Component();
    component.bucketId(EntityHelper.id(bucket));
    component.format("format-id");
    component.attributes(new NestedAttributesMap(P_ATTRIBUTES, new HashMap<>()));
    component.group(group).name(name).version(version);
    return component;
  }

  private Asset createAsset(final String name, final Component component) {
    Asset asset = new Asset();
    asset.bucketId(EntityHelper.id(bucket));
    asset.format("format-id");
    asset.componentId(id(component));
    asset.attributes(new NestedAttributesMap(P_ATTRIBUTES, new HashMap<>()));
    asset.name(name);
    return asset;
  }
}
