/**
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE
 * or https://www.escidoc.org/license/ESCIDOC.LICENSE .
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/ESCIDOC.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *
 * Copyright 2011 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.
 * All rights reserved.  Use is subject to license terms.
 */
package org.escidoc.core.client.ingest.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Vector;

import org.escidoc.core.client.ingest.entities.ResourceEntry;

/**
 * A node representing a file or directory in the local filesystem. After ingest it should hold a {@link ResourceEntry}
 * for the actual ingested resource.
 * 
 * @author Frank Schwichtenberg <http://Frank.Schwichtenberg@FIZ-Karlsruhe.de>
 * 
 */
public class Node {

    private List<Node> leaves;

    private Count count;

    private File file;

    private Node parent;

    private List<Node> children;

    private boolean ingested;

    private ResourceEntry resource;

    public Node(List<Node> leaves, Count count) {
        this.leaves = leaves;
        this.count = count;
        this.count.increment();
    }

    public Node(List<Node> leaves, Count count, Node parent) {
        this.leaves = leaves;
        this.count = count;
        this.count.increment();
        this.parent = parent;
    }

    public Node(List<Node> leaves, Count count, Node parent, File file) {
        this.leaves = leaves;
        this.count = count;
        this.count.increment();
        this.parent = parent;
        this.file = file;
    }

    /**
     * Dive into directory and create a Node hierarchy representing the directory structure.
     * 
     * @throws FileNotFoundException
     *             If the file of this Node does not exist.
     * @throws NullPointerException
     *             If the file or the leaves of this Node are <code>null</code>.
     */
    public void dive() throws FileNotFoundException {
        // check file, leaves
        ensureFile();
        ensureLeaves();

        if (this.file.isDirectory()) {
            this.children = new Vector<Node>();
            File[] children = file.listFiles();
            for (int i = 0; i < children.length; i++) {
                Node n = new Node(this.leaves, this.count, this, children[i]);
                this.children.add(n);
                n.dive();
            }
            if (this.children.isEmpty()) {
                leaves.add(this);
            }

        }
        else {
            leaves.add(this);
        }
    }

    /**
     * @throws FileNotFoundException
     *             If the file of this Node does not exist.
     * @throws NullPointerException
     *             If the file of this Node is <code>null</code>.
     */
    private void ensureFile() throws FileNotFoundException {

        if (this.file == null) {
            throw new NullPointerException("A file must be set before diving.");
        }
        if (!this.file.exists()) {
            throw new FileNotFoundException("The given file does not exist. " + file.getPath());
        }
    }

    /**
     * @throws NullPointerException
     *             If leaves of this Node is <code>null</code>.
     */
    private void ensureLeaves() {
        if (this.leaves == null) {
            throw new NullPointerException("leaves must not be null.");
        }
    }

    /*
     * getter/setter
     */

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public boolean isIngested() {
        return ingested;
    }

    public void setIsIngested(boolean ingested) {
        this.ingested = ingested;
    }

    public ResourceEntry getResource() {
        if (this.resource == null) {
            this.resource = new ResourceEntry();
        }
        return this.resource;
    }

    public void setResource(ResourceEntry resource) {
        this.resource = resource;
    }

}
