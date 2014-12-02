package almightyr;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/*
 @PRIORITY:
 @TODO: Implement getLeafNodesList and getSelectedTreeNodes as part or TreeModel_FullPathExchange.
 */
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

    /**
     * Get a list of all leaf nodes descendant from this node.
     *
     * @param root The root from which descendant leafs are listed.
     * @return A list of the leaf nodes descendant from this node.
     */
    public static List<DefaultMutableTreeNode> getLeafNodesList(DefaultMutableTreeNode root) {
        //@TOTEST: What happens if the specified node is already a leaf?
        if (root.isLeaf()) {
            System.out.println("This is already a leaf!");
        }

        ArrayList<DefaultMutableTreeNode> list = new ArrayList<>();

        Enumeration enumerator = root.breadthFirstEnumeration();
        while (enumerator.hasMoreElements()) {
            DefaultMutableTreeNode e = (DefaultMutableTreeNode) enumerator.nextElement();
            if (e.isLeaf()) {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * @TOJAVADOC
     *
     * @param tree
     * @return A list of the selected nodes.
     */
    public static List<DefaultMutableTreeNode> getSelectedTreeNodes(JTree tree) {
        //@TEST
        int selectionCount = tree.getSelectionCount();
        if (selectionCount == 0) {
            throw new IllegalArgumentException("Notting is selected.");
        }

        List<DefaultMutableTreeNode> nodes = new ArrayList<>();
        for (TreePath path : tree.getSelectionPaths()) {
            nodes.add((DefaultMutableTreeNode) path.getLastPathComponent());
        }
        return nodes;
    }
}
