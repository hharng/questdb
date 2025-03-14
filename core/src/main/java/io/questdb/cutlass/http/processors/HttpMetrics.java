/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2024 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.cutlass.http.processors;

import io.questdb.metrics.Counter;
import io.questdb.metrics.LongGauge;
import io.questdb.metrics.MetricsRegistry;
import io.questdb.std.Mutable;

public class HttpMetrics implements Mutable {
    private final Counter listenerStateChangeCounter;
    private final LongGauge connectionCountGauge;

    public HttpMetrics(MetricsRegistry metricsRegistry) {
        this.connectionCountGauge = metricsRegistry.newLongGauge("http_connections");
        this.listenerStateChangeCounter = metricsRegistry.newCounter("http_listener_state_change_count");
    }

    @Override
    public void clear() {
        connectionCountGauge.setValue(0);
        listenerStateChangeCounter.reset();
    }

    public Counter listenerStateChangeCounter() {
        return listenerStateChangeCounter;
    }

    public LongGauge connectionCountGauge() {
        return connectionCountGauge;
    }
}
