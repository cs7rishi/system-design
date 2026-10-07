import java.util.*;

public class Q1.6_BinaryTreeSerializer {
    public static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int x) { val = x; }
    }

    public String serialize(TreeNode root) {
        if (root == null) return "null";
        StringBuilder sb = new StringBuilder();
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            if (node == null) {
                sb.append("null,");
            } else {
                sb.append(node.val).append(",");
                queue.add(node.left);
                queue.add(node.right);
            }
        }
        return sb.toString();
    }

    public TreeNode deserialize(String data) {
        if (data.equals("null")) return null;
        String[] parts = data.split(",");
        TreeNode root = new TreeNode(Integer.parseInt(parts[0]));
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        
        int i = 1;
        while (!queue.isEmpty() && i < parts.length) {
            TreeNode current = queue.poll();
            if (!parts[i].equals("null")) {
                current.left = new TreeNode(Integer.parseInt(parts[i]));
                queue.add(current.left);
            }
            i++;
            if (i < parts.length && !parts[i].equals("null")) {
                current.right = new TreeNode(Integer.parseInt(parts[i]));
                queue.add(current.right);
            }
            i++;
        }
        return root;
    }
}