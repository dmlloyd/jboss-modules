/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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

package org.jboss.modules;

import static java.security.AccessController.doPrivileged;

import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

final class SharedClassCacheFactoryHolder {
    static final SharedClassCacheFactory INSTANCE;

    static {
        INSTANCE = doPrivileged(new PrivilegedAction<SharedClassCacheFactory>() {
            public SharedClassCacheFactory run() {
                final ServiceLoader<SharedClassCacheFactory> loader = ServiceLoader.load(SharedClassCacheFactory.class, SharedClassCacheFactoryHolder.class.getClassLoader());
                final Iterator<SharedClassCacheFactory> iterator = loader.iterator();
                for (;;) try {
                    if (! iterator.hasNext()) break;
                    return iterator.next();
                } catch (ServiceConfigurationError ignored) {}
                return SharedClassCacheFactory.EMPTY;
            }
        });
    }

    private SharedClassCacheFactoryHolder() {
    }
}
