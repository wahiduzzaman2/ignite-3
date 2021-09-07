/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.runner.app;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.ignite.app.Ignite;
import org.apache.ignite.app.IgnitionManager;
import org.apache.ignite.internal.app.IgniteImpl;
import org.apache.ignite.internal.schema.configuration.SchemaConfigurationConverter;
import org.apache.ignite.internal.util.IgniteUtils;
import org.apache.ignite.schema.ColumnType;
import org.apache.ignite.schema.SchemaBuilders;
import org.apache.ignite.schema.SchemaTable;

/**
 * Helper class for non-Java platform tests (.NET, C++, Python, ...).
 * Starts nodes, populates tables and data for tests.
 */
public class PlatformTestNodeRunner {
    /** */
    private static final String SCHEMA_NAME = "PUB";

    /** */
    private static final String TABLE_NAME = "tbl1";

    /** Nodes bootstrap configuration. */
    private static final Map<String, String> nodesBootstrapCfg = new LinkedHashMap<>() {{
        put("node0", "{\n" +
                "  \"node\": {\n" +
                "    \"metastorageNodes\":[ \"node0\" ]\n" +
                "  },\n" +
                "  \"clientConnector\":{\"port\": 10942,\"portRange\":10}," +
                "  \"network\": {\n" +
                "    \"port\":3344,\n" +
                "    \"netClusterNodes\":[ \"localhost:3344\", \"localhost:3345\" ]\n" +
                "  }\n" +
                "}");
    }};

    /** Base path for all temporary folders. */
    private static final Path BASE_PATH = Path.of("target", "work", "PlatformTestNodeRunner");

    /**
     * Entry point.
     * @param args Args.
     */
    public static void main(String[] args) throws Exception {
        IgniteUtils.deleteIfExists(BASE_PATH);
        Files.createDirectories(BASE_PATH);

        List<Ignite> startedNodes = new ArrayList<>();

        nodesBootstrapCfg.forEach((nodeName, configStr) ->
                startedNodes.add(IgnitionManager.start(nodeName, configStr, BASE_PATH.resolve(nodeName)))
        );

        var keyCol = "key";
        var valCol = "val";

        SchemaTable schTbl = SchemaBuilders.tableBuilder(SCHEMA_NAME, TABLE_NAME).columns(
                SchemaBuilders.column(keyCol, ColumnType.INT32).asNonNull().build(),
                SchemaBuilders.column(valCol, ColumnType.string()).asNullable().build()
        ).withPrimaryKey(keyCol).build();

        startedNodes.get(0).tables().createTable(schTbl.canonicalName(), tblCh ->
                SchemaConfigurationConverter.convert(schTbl, tblCh)
                        .changeReplicas(1)
                        .changePartitions(10)
        );

        String ports = startedNodes.stream()
                .map(n -> String.valueOf(getPort((IgniteImpl)n)))
                .collect(Collectors.joining (","));

        System.out.println("THIN_CLIENT_PORTS=" + ports);

        Thread.sleep(Long.MAX_VALUE);
    }

    /**
     * Gets the thin client port.
     * @param node Node.
     * @return Port number.
     */
    private static int getPort(IgniteImpl node) {
        return ((InetSocketAddress)node.clientHandlerModule().localAddress()).getPort();
    }
}