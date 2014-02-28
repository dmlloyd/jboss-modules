/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.modules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class CacheURLResourceLoader extends AbstractResourceLoader {

    private final URL url;
    private final File localCache;
    private final AtomicReference<ResourceLoader> local = new AtomicReference<>();
    private final String rootName;
    private final long maximumContentLength;

    /**
     * Construct a new instance.
     *
     * @param url the URL of the remote resource root.
     * @param localCache the file path to the local cache.
     * @param rootName
     * @param maximumContentLength
     */
    CacheURLResourceLoader(final URL url, final File localCache, final String rootName, final long maximumContentLength) {
        this.url = url;
        this.localCache = localCache;
        this.rootName = rootName;
        this.maximumContentLength = maximumContentLength;
    }

    private ResourceLoader getDelegate() {
        AtomicReference<ResourceLoader> local = this.local;
        ResourceLoader resourceLoader = local.get();
        if (resourceLoader == null) {
            synchronized (local) {
                resourceLoader = local.get();
                if (resourceLoader == null) {
                    resourceLoader = download();
                    local.set(resourceLoader);
                }
            }
        }
        return resourceLoader;
    }

    private ResourceLoader download() {
        assert Thread.holdsLock(local);
        URLConnection connection;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            return EmptyResourceLoader.INSTANCE;
        }
        if (connection instanceof HttpURLConnection) {
            ((HttpURLConnection) connection).setInstanceFollowRedirects(false);
        }
        connection.setUseCaches(false);
        String contentType = connection.getContentType();
        if (contentType == null) {
            try {
                connection.getInputStream().close();
            } catch (Throwable ignored) {}
            return EmptyResourceLoader.INSTANCE;
        }
        long length = connection.getContentLengthLong();
        if (length > maximumContentLength) {
            try {
                connection.getInputStream().close();
            } catch (Throwable ignored) {}
            return EmptyResourceLoader.INSTANCE;
        }
        contentType = contentType.toLowerCase(Locale.US);
        switch (contentType) {
            case "application/java-archive":
            case "application/x-jar":
            case "application/x-java-jar": {
                try {
                    try (InputStream inputStream = connection.getInputStream()) {
                        try (OutputStream outputStream = new FileOutputStream(localCache)) {
                            StreamUtil.copy(inputStream, outputStream);
                        }
                    }
                } catch (IOException e) {
                    return EmptyResourceLoader.INSTANCE;
                }
                try {
                    return new JarFileResourceLoader(rootName, new JarFile(localCache), null, url);
                } catch (IOException e) {
                    return EmptyResourceLoader.INSTANCE;
                }
            }
            default: {
                try {
                    connection.getInputStream().close();
                } catch (Throwable ignored) {}
                // todo: support file downloading
                return EmptyResourceLoader.INSTANCE;
            }
        }
    }

    public String getRootName() {
        return getDelegate().getRootName();
    }

    public ClassSpec getClassSpec(final String fileName) throws IOException {
        return getDelegate().getClassSpec(fileName);
    }

    public PackageSpec getPackageSpec(final String name) throws IOException {
        return getDelegate().getPackageSpec(name);
    }

    public Resource getResource(final String name) {
        return getDelegate().getResource(name);
    }

    public String getLibrary(final String name) {
        return getDelegate().getLibrary(name);
    }

    public Collection<String> getPaths() {
        return getDelegate().getPaths();
    }
}
