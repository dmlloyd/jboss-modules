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

import java.util.function.Supplier;

/**
 * A factory for shared class cache instances.
 */
public interface SharedClassCacheFactory {
    /**
     * Get or create the unique shared class cache instance for the given class loader.  The implementation must have the
     * {@code getClassLoader} {@link RuntimePermission} in order to call the supplier.
     *
     * @param classLoaderSupplier the class loader supplier (must not be {@code null})
     * @return the shared class cache (must not be {@code null})
     * @throws SecurityException if a security manager is installed and the caller does not have the necessary permissions
     */
    SharedClassCache getCache(Supplier<ClassLoader> classLoaderSupplier) throws SecurityException;

    /**
     * Discover or retrieve the singleton shared class cache factory instance.
     *
     * @return the shared class cache factory instance
     * @throws SecurityException if a security manager is installed and the caller does not have the necessary permissions
     */
    static SharedClassCacheFactory getInstance() throws SecurityException {
        return SharedClassCacheFactoryHolder.INSTANCE;
    }

    /**
     * A shared class cache factory which is always empty.  This is the cache factory used when no shared class provider is found.
     */
    SharedClassCacheFactory EMPTY = new SharedClassCacheFactory() {
        public SharedClassCache getCache(final Supplier<ClassLoader> classLoaderSupplier) throws SecurityException {
            return SharedClassCache.EMPTY;
        }
    };
}
