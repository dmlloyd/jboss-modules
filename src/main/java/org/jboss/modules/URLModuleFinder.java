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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.AccessControlContext;
import java.security.AccessController;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class URLModuleFinder implements ModuleFinder {
    private final URL rootUrl;
    private final File cachePath;
    private final AccessControlContext accessControlContext = AccessController.getContext();
    private final long maximumContentLength;

    URLModuleFinder(final URL rootUrl, final File cachePath, final long maximumContentLength) {
        this.maximumContentLength = maximumContentLength;
        if (rootUrl == null) {
            throw new IllegalArgumentException("rootUrls is null");
        }
        if (cachePath == null) {
            throw new IllegalArgumentException("cachePath is null");
        }
        String protocol;
        protocol = rootUrl.getProtocol();
        if ("http".equalsIgnoreCase(protocol) || "ftp".equalsIgnoreCase(protocol)) {
            Module.getModuleLogger().trace("Using insecure module root URL '%s'", rootUrl);
        }
        this.rootUrl = rootUrl;
        this.cachePath = cachePath;
    }

    static String toURLString(final ModuleIdentifier identifier) {
        try {
            return URLEncoder.encode(identifier.getName().replace('.', '/') + "/" + identifier.getSlot(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException();
        }
    }

    public ModuleSpec findModule(final ModuleIdentifier identifier, final ModuleLoader delegateLoader) throws ModuleLoadException {
        String pathString = LocalModuleFinder.toPathString(identifier);
        final File cacheModulePath = new File(cachePath, pathString);
        if (! cacheModulePath.exists()) {
            cacheModulePath.mkdirs();
        }
        File moduleXml = new File(cacheModulePath, "module.xml");
        final URL subUrl;
        try {
            subUrl = new URL(rootUrl, toURLString(identifier));
        } catch (MalformedURLException e) {
            throw new ModuleLoadException("Invalid module URL", e);
        }
        if (! moduleXml.exists()) {
            URLConnection connection;
            try {
                connection = subUrl.openConnection();
                connection.setUseCaches(false);
                try (InputStream inputStream = connection.getInputStream()) {
                    try (OutputStream outputStream = new FileOutputStream(moduleXml)) {
                        StreamUtil.copy(inputStream, outputStream);
                    }
                }
            } catch (IOException e) {
                throw new ModuleLoadException("Failed to download module content", e);
            }
        }
        try {
            return ModuleXmlParser.parseModuleXml(new ModuleXmlParser.ResourceRootFactory() {
                public ResourceLoader createResourceLoader(final String rootPath, final String loaderPath, final String loaderName) throws IOException {
                    assert cacheModulePath.getAbsolutePath().equals(rootPath);
                    return new CacheURLResourceLoader(subUrl, cacheModulePath, loaderName, maximumContentLength);
                }
            }, delegateLoader, identifier, cacheModulePath, moduleXml, accessControlContext);
        } catch (IOException e) {
            throw new ModuleLoadException("Failed to read descriptor", e);
        }

    }
}
