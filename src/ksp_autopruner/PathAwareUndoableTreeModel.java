package ksp_autopruner;

import java.util.Arrays;
import java.util.Enumeration;
import javax.swing.event.UndoableEditListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

/**
 * @author Rodrigo Legendre Lima Rodrigues
 */
public class PathAwareUndoableTreeModel extends DefaultTreeModel {

    //<editor-fold defaultstate="collapsed" desc="Undo/Redo support">
    UndoableEditSupport editListeners = new UndoableEditSupport();

    public void addUndoableEditListener(UndoableEditListener uel) {
        editListeners.addUndoableEditListener(uel);
    }

    public void removeUndoableEditListener(UndoableEditListener uel) {
        editListeners.removeUndoableEditListener(null);
    }

    @Override
    public void insertNodeInto(MutableTreeNode newChild, MutableTreeNode parent, int index) {
        //Execute the insertion
        super.insertNodeInto(newChild, parent, index);

        //Create and post an UndoableEdit for the change
        UndoableEdit edit = new NodeAddEdit(parent, newChild, index);
        System.out.println(edit);
        editListeners.postEdit(edit);
    }

    @Override
    public void removeNodeFromParent(MutableTreeNode node) {
        //If node is root, do nothing.
        if (node.getParent() == null) {
            System.out.println("!!!");
            return;
        }

        //Get a reference to the child's index and parent.
        MutableTreeNode parent = (MutableTreeNode) node.getParent();
        int index = parent.getIndex(node);

        //Remove the node from its parent.
        super.removeNodeFromParent(node);

        //Create and post an UndoableEdit for the change.
        UndoableEdit edit = new NodeRemoveEdit(parent, node, index);
        editListeners.postEdit(edit);

        //Remove parent if it became empty after this node's removal.
        if (parent.getChildCount() == 0) {
            removeNodeFromParent(parent);
        }
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        //Save the old value of the node being changed
        MutableTreeNode aNode = (MutableTreeNode) path.getLastPathComponent();
        Object oldValue = ((DefaultMutableTreeNode) aNode).getUserObject();

        //Create an undoable edit object for this change
        UndoableEdit edit = new NodeChangeEdit(path, oldValue, newValue);
        editListeners.postEdit(edit);

        //Execute the change of value of the tree node
        super.valueForPathChanged(path, newValue);
    }

    //<editor-fold defaultstate="collapsed" desc="UndoableEdit classes">
    public class NodeAddEdit extends AbstractUndoableEdit {

        MutableTreeNode child;
        MutableTreeNode parent;
        int index;

        public NodeAddEdit(MutableTreeNode child, MutableTreeNode parent, int index) {
            this.child = child;
            this.parent = parent;
            this.index = index;
        }

        @Override
        public String getPresentationName() {
            return "Add [" + child.toString() + "] to [" + parent.toString() + "] at index [" + index + "].";
        }

        @Override
        public void redo() throws CannotRedoException {
            //Invoke super.redo() to make sure we can redo
            super.redo();

            //Re-add the child to the parent at the specified index
            parent.insert(child, index);

            //Notify any listeners that the node was readded
            int[] childIndices = {index};
            nodesWereInserted(parent, childIndices);
        }

        @Override
        public void undo() throws CannotUndoException {
            //Invoke super.undo() to make sure we can undo
            super.undo();

            //Re-add the child to the parent at the specified index
            parent.remove(index);

            //Notify any listeners that the node was readded
            int[] childIndices = {index};
            Object[] removedChildren = {child};
            nodesWereRemoved(parent, childIndices, removedChildren);
        }
    }

    public class NodeRemoveEdit extends AbstractUndoableEdit {

        MutableTreeNode child;
        MutableTreeNode parent;
        int index;

        public NodeRemoveEdit(MutableTreeNode child, MutableTreeNode parent, int index) {
            this.child = child;
            this.parent = parent;
            this.index = index;
        }

        @Override
        public String getPresentationName() {
            return "Remove [" + child.toString() + "] from [" + parent.toString() + "] at index [" + index + "].";
        }

        @Override
        public void redo() throws CannotRedoException {
            //Invoke super.redo() to make sure we can redo
            super.redo();

            //Remove the child from its parent, again
            parent.remove(index);

            //Notify all listeners that the child was removed again
            int[] childIndex = {index};
            Object[] childObj = {child};
            nodesWereRemoved(parent, childIndex, childObj);
        }

        @Override
        public void undo() throws CannotUndoException {
            //Invoke super.undo() to make sure we can undo
            super.undo();

            //Put the child back with its parent
            parent.insert(child, index);

            //Notify all listeners that the node was readded
            int[] childIndex = {index};
            nodesWereInserted(parent, childIndex);
        }
    }

    public class NodeChangeEdit extends AbstractUndoableEdit {

        TreePath path;
        Object oldValue;
        Object newValue;

        public NodeChangeEdit(TreePath path, Object oldValue, Object newValue) {
            this.path = path;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        @Override
        public String getPresentationName() {
            return "Change [" + path.toString() + "].";
        }

        @Override
        public void redo() throws CannotRedoException {
            //Invoke super.redo() to make sure we can redo
            super.redo();

            //Set the node's user object to the its new value
            MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();
            node.setUserObject(newValue);

            //Notify any listeners that the tree has changed
            nodeChanged(node);
        }

        @Override
        public void undo() throws CannotUndoException {
            //Invoke super.undo() to make sure we can undo
            super.undo();

            //Set the node's user object to its old value
            MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();
            node.setUserObject(oldValue);

            //Notify any listeners that the tree has changed
            nodeChanged(node);
        }
    }
//</editor-fold>

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public PathAwareUndoableTreeModel(TreeNode root) {
        super(root);
    }

    public PathAwareUndoableTreeModel(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
    }
//</editor-fold>

    /**
     * Insert the new node by reproducing it's existing path (except for it's
     * original root).
     *
     * @param newNode The node to be inserted.
     */
    public void insertNode(DefaultMutableTreeNode newNode) {
        DefaultMutableTreeNode insertionNode = getInsertionNode(newNode);
        insertNodeInto(newNode, insertionNode);
    }

    /**
     * Inserts <code>newChild</code> at the end of <code>parent</code>'s
     * children array. This will then message <code>nodesWereInserted</code> to
     * create the appropriate event. This is the preferred way to add children,
     * as it will create the appropriate event.
     *
     * @param newChild The node that will be inserted.
     * @param parent The parent node to which <code>newChild</code> will be
     * inserted into.
     */
    public void insertNodeInto(MutableTreeNode newChild, MutableTreeNode parent) {
        int childCount = parent.getChildCount();
        insertNodeInto(newChild, parent, childCount);
    }

    /**
     * @TOJAVADOC @param targetNode
     * @return
     */
    public DefaultMutableTreeNode getReusableNode(DefaultMutableTreeNode targetNode) {
        return getReusableNode((DefaultMutableTreeNode) root, targetNode);
    }

    /**
     * @TOJAVADOC @param currentNode
     * @param targetNode
     * @return
     */
    protected DefaultMutableTreeNode getReusableNode(DefaultMutableTreeNode currentNode, DefaultMutableTreeNode targetNode) {
        if (currentNode.getLevel() == targetNode.getLevel()) {
            if (currentNode.toString().equals(targetNode.toString())) {
                return currentNode;
            }
        } else {
            Enumeration children = currentNode.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode e = (DefaultMutableTreeNode) children.nextElement();

                if (e.toString().equals(targetNode.getPath()[e.getLevel()].toString())) {
                    return getReusableNode(e, targetNode);
                }
            }
        }
        return currentNode;
    }

    /**
     * @TOJAVADOC @param currentNode
     * @param targetNode
     * @return
     */
    protected DefaultMutableTreeNode getInsertionNode(DefaultMutableTreeNode currentNode, DefaultMutableTreeNode targetNode) {
        TreeNode[] targetPath = targetNode.getPath();

        int currentLevel = currentNode.getPath().length - 1;
        int targetLevel = targetPath.length - 2;
        while (currentLevel < targetLevel) {
            DefaultMutableTreeNode lastAdded = new DefaultMutableTreeNode(targetPath[currentLevel + 1]);
            insertNodeInto(lastAdded, currentNode);
            currentNode = lastAdded;
            currentLevel++;
        }
        return currentNode;
    }

    /**
     * @TOJAVADOC @param targetNode
     * @return
     */
    public DefaultMutableTreeNode getInsertionNode(DefaultMutableTreeNode targetNode) {
        DefaultMutableTreeNode reusableNode = getReusableNode(targetNode);
        if (reusableNode.getLevel() == targetNode.getLevel()) {
            return (DefaultMutableTreeNode) reusableNode.getParent();
        } else if (reusableNode.getLevel() == targetNode.getLevel() - 1) {
            return reusableNode;
        } else {
            return getInsertionNode(reusableNode, targetNode);
        }
    }
}
