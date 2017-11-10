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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.jboss.modules.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class FileSystemClassPathModuleLoaderTest {
    private FileSystemClassPathModuleFinder moduleFinder;
    private ModuleLoader moduleLoader;

    @Before
    public void setup() throws Exception {
        final File repoRoot = Util.getResourceFile(getClass(), "test/repo");
        ModuleLoader localModuleLoader = new LocalModuleLoader(new File[] {repoRoot});
        moduleFinder = new FileSystemClassPathModuleFinder(localModuleLoader);
        moduleLoader = new ModuleLoader(moduleFinder);
    }

    @Test
    public void testModuleLoadFromFileSystemModule() throws Exception {
        final File resourceFile = Util.getResourceFile(getClass(), "test/filesystem-module-1");
        final Module module = moduleLoader.loadModule(resourceFile.getAbsoluteFile().getCanonicalFile().toString());
        final URL exportedResource = module.getExportedResource("META-INF/services/javax.ws.rs.ext.Providers");
        Assert.assertNotNull(exportedResource);
    }

    @Test
    public void testModuleLoadFromFileSystemJarModule() throws Exception {
        final File resourceFile = Util.getResourceFile(getClass(), "test/filesystem-module-1");
        final File jarFileName = new File(resourceFile.getParentFile(), "filesystem-module-1.jar");
        final Module module;
        final URL exportedResource;
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFileName))) {
            add(resourceFile.getPath(), resourceFile, jos);
        }
        module = moduleLoader.loadModule(jarFileName.getAbsoluteFile().getCanonicalFile().toString());
        exportedResource = module.getExportedResource("META-INF/services/javax.ws.rs.ext.Providers");
        Assert.assertNotNull(exportedResource);
    }

    private static void add(final String sourceBase, final File source, final JarOutputStream target) throws IOException {
        BufferedInputStream in = null;
        String entryName = source.getPath().replace(sourceBase, "").replace("\\", "/");
        while (entryName.startsWith("/"))
            entryName = entryName.substring(1);
        try {
            if (source.isDirectory()) {
                if (!entryName.isEmpty()) {
                    if (!entryName.endsWith("/"))
                        entryName += "/";
                    final JarEntry entry = new JarEntry(entryName);
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                for (File nestedFile : source.listFiles())
                    add(sourceBase, nestedFile, target);
                return;
            }

            final JarEntry entry = new JarEntry(entryName);
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));

            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = in.read(buffer)) != -1) {
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        }
        finally {
            if (in != null) in.close();
        }
    }
}
