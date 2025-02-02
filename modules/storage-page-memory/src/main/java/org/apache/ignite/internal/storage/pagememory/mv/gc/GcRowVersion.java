/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.storage.pagememory.mv.gc;

import org.apache.ignite.internal.hlc.HybridTimestamp;
import org.apache.ignite.internal.storage.RowId;

/**
 * Row version in the version chain that should be garbage collected.
 */
public class GcRowVersion {
    private final RowId rowId;

    private final HybridTimestamp timestamp;

    private final long link;

    /**
     * Constructor.
     *
     * @param rowId Row ID.
     * @param timestamp Row timestamp.
     * @param link Row version link.
     */
    public GcRowVersion(RowId rowId, HybridTimestamp timestamp, long link) {
        this.rowId = rowId;
        this.timestamp = timestamp;
        this.link = link;
    }

    /**
     * Returns row ID.
     */
    public RowId getRowId() {
        return rowId;
    }

    /**
     * Returns row timestamp.
     */
    public HybridTimestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Returns row version link.
     */
    public long getLink() {
        return link;
    }
}
