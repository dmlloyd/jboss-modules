/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import java.util.List;
import java.util.Map;

/**
 * A pair of path maps.
 *
 * @param <T> the type of object that each path refers to
 * @param <A> the type of the source object used to calculate the path maps
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class Paths<T, A> implements Cloneable {
    private final A[] sourceList;
    private final Map<String, List<T>> allPaths;
    private final Map<String, List<T>> exportedPaths;

    Paths(final A[] sourceList, final Map<String, List<T>> allPaths, final Map<String, List<T>> exportedPaths) {
        this.sourceList = sourceList;
        this.allPaths = allPaths;
        this.exportedPaths = exportedPaths;
    }

    Map<String, List<T>> getAllPaths() {
        return allPaths;
    }

    Map<String, List<T>> getExportedPaths() {
        return exportedPaths;
    }

    Map<String, List<T>> getPaths(boolean export) {
        return export ? exportedPaths : allPaths;
    }

    A[] getSourceList(A[] defVal) {
        final A[] sourceList = this.sourceList;
        return sourceList == null ? defVal : sourceList;
    }

    static final Paths<?, ?> NONE = new Paths<Object, Object>(null, null, null);

    @SuppressWarnings({ "unchecked" })
    static <T, A> Paths<T, A> none() {
        return (Paths<T, A>) NONE;
    }

    @SuppressWarnings({ "unchecked" })
    protected Paths<T, A> clone() {
        try {
            return (Paths<T, A>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
