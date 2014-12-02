/*
 * By Rodrigo Legendre Lima Rodrigues
 */
package almightyr;

import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import static almightyr.Util.getLeafNodesList;
import javax.swing.tree.TreePath;

/**
 *
 * @author Rodrigo Legendre Lima Rodrigues
 */
public class TreeModel_FullPathExchange extends DefaultTreeModel {

    private DefaultMutableTreeNode lastAddedChild;
    private DefaultMutableTreeNode lastOutgoingNode;

    public DefaultMutableTreeNode getLastAddedChild() {
        return lastAddedChild;
    }

    public DefaultMutableTreeNode getLastOutgoingNode() {
        return lastOutgoingNode;
    }

    @Override
    public void insertNodeInto(MutableTreeNode newChild, MutableTreeNode parent, int index) {
        super.insertNodeInto(newChild, parent, index);
        lastAddedChild = (DefaultMutableTreeNode) newChild;
    }

    public DefaultMutableTreeNode getNodeFromPath(TreePath path) {
        return null;
    }

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public TreeModel_FullPathExchange(TreeNode root) {
        super(root);
    }

    public TreeModel_FullPathExchange(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
    }
//</editor-fold>

    public void exchangeNodeOut(DefaultMutableTreeNode node, TreeModel_FullPathExchange model) {
        if (node.isRoot()) {
            //@NOTE: Either exchange all children, or consider it an error and throw an IllegalArgumentException.
            Enumeration children = node.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode e = (DefaultMutableTreeNode) children.nextElement();
                exchangeNodeOut(e, model);
            }
        }

        if (node.isLeaf()) {
            //Insert a clone of the node into the destination model, creating the node's ascendants' nodes if necessary.
            DefaultMutableTreeNode insertionNode = model.getInsertionNode(node);
            model.insertNodeInto((MutableTreeNode) node.clone(), insertionNode, insertionNode.getChildCount());
            lastOutgoingNode = model.getReusableNode(node);
        } else {
            //Insert all leafs into the destination model, creating the node's ascendants' nodes if necessary.
            for (DefaultMutableTreeNode aLeaf : getLeafNodesList(node)) {
                DefaultMutableTreeNode insertionNode = model.getInsertionNode(aLeaf);
                model.insertNodeInto(aLeaf, insertionNode, insertionNode.getChildCount());
            }
            lastOutgoingNode = model.getReusableNode(node);
        }
        //Remove the original node from this model.
        removeNodeFromParent(node);
    }

    public void exchangeNodeOut(DefaultMutableTreeNode node, JTree tree) {
        TreeModel model = tree.getModel();
        if (model instanceof TreeModel_FullPathExchange) {
            exchangeNodeOut(node, (TreeModel_FullPathExchange) model);
        } else {
            throw new IllegalArgumentException("Can only exchange to other TreeModel_FullPathExchange tree models.");
        }
    }

    /**
     * Removes the specified node from it's parent, and recursively removes
     * parents that become leafs (empty).
     *
     * @param node The node to be removed.
     */
    public void removeNodeFromParent(DefaultMutableTreeNode node) {
        if (!node.isRoot()) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
            super.removeNodeFromParent(node);
            if (parent.isLeaf()) {
                removeNodeFromParent(parent);
            }
        }
    }

    protected DefaultMutableTreeNode getReusableNode(DefaultMutableTreeNode node, DefaultMutableTreeNode current) {
        if (current.getLevel() == node.getLevel()) {
            if (current.toString().equals(node.toString())) {
                return current;
            } else {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) current.getParent();
                if (current.isRoot()) {
                    return current;
                } else {
                    return getReusableNode(node, parent);
                }
            }
        } else {
            Enumeration children = current.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode e = (DefaultMutableTreeNode) children.nextElement();

                if (e.toString().equals(node.getPath()[e.getLevel()].toString())) {
                    return getReusableNode(node, e);
                }
            }
        }
        return current;
    }

    public DefaultMutableTreeNode getReusableNode(DefaultMutableTreeNode node) {
        return getReusableNode(node, (DefaultMutableTreeNode) root);
    }

    protected DefaultMutableTreeNode getInsertionNode(DefaultMutableTreeNode node, DefaultMutableTreeNode current) {
        DefaultMutableTreeNode reusableNode = getReusableNode(node, current);
        if (reusableNode.getLevel() == node.getLevel()) {
            return (DefaultMutableTreeNode) reusableNode.getParent();
        } else if (reusableNode.getLevel() == node.getLevel() - 1) {
            return reusableNode;
        } else {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node.getPath()[reusableNode.getLevel() + 1]);
            insertNodeInto(newNode, reusableNode, reusableNode.getChildCount());
            return getInsertionNode(node, newNode);
        }
    }

    public DefaultMutableTreeNode getInsertionNode(DefaultMutableTreeNode node) {
        return getInsertionNode(node, (DefaultMutableTreeNode) root);
    }
}
