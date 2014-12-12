package ksp_autopruner;

import java.io.File;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Rodrigo Legendre Lima Rodrigues
 */
public class Util {

    /**
     * Create a node with the same name as the specified file. Possibly
     * including it's children.
     *
     * @param file The file to create a node for.
     * @param includeChildren Whether to recursively include the file's children
     * as child nodes.
     * @return A DefaultMutableTreeNode equivalent to the specified file.
     */
    public static DefaultMutableTreeNode getNodeFromFile(File file, boolean includeChildren) {
        if (file == null) {
            throw new IllegalArgumentException("Received null file.");
        }
        
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(file.getName());
        if (file.isDirectory() && includeChildren) {
            for (File childFile : file.listFiles()) {
                node.add(getNodeFromFile(childFile, true));
            }
        }
        return node;
    }

    /**
     * Get a node equivalent to the specified file, nesting the file's children
     * as child nodes.
     *
     * @param file The file to create a node for.
     * @return A DefaultMutableTreeNode equivalent to the specified file,
     * nesting it's children.
     */
    public static DefaultMutableTreeNode getNodeFromFile(File file) {
        return getNodeFromFile(file, true);
    }
}
