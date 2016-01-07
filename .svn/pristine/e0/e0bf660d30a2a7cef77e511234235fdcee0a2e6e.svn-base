/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pinyin;

// TODO: Auto-generated Javadoc
/**
 * The Class StandardTree.
 *
 * @author gxm
 */
public class StandardTree {

    /** The Constant POINTS_SIZE. */
    public static final int POINTS_SIZE = 27;
    
    /** The root. */
    private TrieNode root = new BranchNode(' ');

    /**
     * Insert.
     *
     * @param word the word
     */
    public void insert(String word) {
        TrieNode curNode = root;
        word = word + "$";
        char[] chars = word.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '$') {
                curNode.points[POINTS_SIZE - 1] = new LeafNode('$');
            } else {
                int pos = chars[i] - 'a';
                try {
                    if (curNode.points[pos] == null) {
                        curNode.points[pos] = new BranchNode(chars[i]);
                        curNode = curNode.points[pos];
                    } else {
                        curNode = curNode.points[pos];
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("the error is :" + String.valueOf(chars[i]) + "  " + pos + "  " + word);
                }
            }
        }
    }

    /**
     * Full match.
     *
     * @param word the word
     * @return true, if successful
     */
    public boolean fullMatch(String word) {

        TrieNode curNode = root;
        char[] chars = word.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int pSize = chars[i] - 'a';
            if (curNode.points[pSize] == null) {
                return false;
            } else {
                curNode = curNode.points[pSize];
                if ((i == chars.length - 1)) {
                    curNode = curNode.points[POINTS_SIZE - 1];
                    if (curNode != null && curNode.key == '$') {
                        return true;
                    }
                }
            }
        }
        return false;
    }

//    private void preRootTraverse(TrieNode curNode) {
//        if (curNode != null) {
//            System.out.println(curNode.key + " ");
//            if (curNode.kind == NodeKind.BN) {
//                for (TrieNode childNode : curNode.points) {
//                    preRootTraverse(childNode);
//                }
//            }
//        }
//    }
    /*
     * 得到Trie根结点
     */
//    public TrieNode getRoot() {
//        return root;
//    }
    /**
 * The Enum NodeKind.
 */
enum NodeKind {

        /** The ln. */
        LN, /** The bn. */
 BN
    };

    /**
     * Trie 节点.
     */
    class TrieNode {

        /** The key. */
        char key;
        
        /** The points. */
        TrieNode[] points = null;
        
        /** The kind. */
        NodeKind kind = null;
    }

    /**
     * The Class LeafNode.
     */
    class LeafNode extends TrieNode {

        /**
         * Instantiates a new leaf node.
         *
         * @param k the k
         */
        LeafNode(char k) {
            super.key = k;
            super.kind = NodeKind.LN;
        }
    }

    /**
     * Trie 内部结点.
     */
    class BranchNode extends TrieNode {

        /**
         * Instantiates a new branch node.
         *
         * @param k the k
         */
        BranchNode(char k) {
            super.key = k;
            super.kind = NodeKind.BN;
            super.points = new TrieNode[POINTS_SIZE];
        }
    }
}
