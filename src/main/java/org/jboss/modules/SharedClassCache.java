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

import java.net.URL;

/**
 * A shared class cache that can be used to load special shared class bytes.
 */
public interface SharedClassCache {
    /**
     * Find a shared class by class name and by token.  An unspecified security manager permission may be necessary to read from the cache.
     *
     * @param url the class resource URL (must not be {@code null})
     * @param className the class name (must not be {@code null})
     * @return the special shared class bytes, or {@code null} if the class is not found in the cache or if a security
     *     manager is present and it denies access to the cache
     */
    byte[] findSharedClass(URL url, String className);

    /**
     * Store a shared class by class name and by token.  An unspecified security manager permission may be necessary to write to the cache.
     *
     * @param url the class resource URL (must not be {@code null})
     * @param className the class name (must not be {@code null})
     * @return {@code true} if the class bytes were stored in the cache, {@code false} if the bytes were not stored
     *     or if a security manager is present and it denies access to the cache
     */
    boolean storeSharedClass(URL url, Class<?> clazz);

    /**
     * A shared class cache which is always empty.  This is the cache used when no shared class provider is found.
     */
    SharedClassCache EMPTY = new SharedClassCache() {
        public byte[] findSharedClass(final URL url, final String className) {
            return null;
        }

        public boolean storeSharedClass(final URL url, final Class<?> clazz) {
            return false;
        }
    };
}
