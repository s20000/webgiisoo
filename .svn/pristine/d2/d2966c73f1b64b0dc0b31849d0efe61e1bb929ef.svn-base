package com.giisoo.core.stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.giisoo.core.bean.X;
import com.giisoo.core.stat.Tree.Attribute;
import com.giisoo.core.worker.WorkerTask;

public class ID3 extends WorkerTask {

    public boolean done = false;

    private List<String> attributes;
    private Map<String, List<Object>> attributevalue = new HashMap<String, List<Object>>(); // 存储每个属性的取值
    String decatt; // 决策变量在属性集中的索引

    List<Map<String, Object>> data;

    Tree root = new Tree();

    // Map<String, Map<String, Object>> results = new HashMap<String,
    // Map<String, Object>>();

    public ID3(List<Map<String, Object>> data, List<String> attributes, String decatt) {
        this.data = data;
        this.attributes = attributes;
        this.decatt = decatt;
    }

    @Override
    public void onExecute() {

        LinkedList<String> ll = new LinkedList<String>();
        ll.addAll(attributes);
        ll.remove(decatt);

        ArrayList<Integer> al = new ArrayList<Integer>();
        for (int i = 0; i < data.size(); i++) {
            al.add(i);
        }

        buildDT(root, al, ll);

    }

    @Override
    public void onFinish() {
        done = true;
    }

    public void setDec(String name) {
        decatt = name;
    }

    // 给一个样本（数组中是各种情况的计数），计算它的熵
    public double getEntropy(int[] arr) {
        double entropy = 0.0;
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            entropy -= arr[i] * Math.log(arr[i] + Double.MIN_VALUE) / Math.log(2);
            sum += arr[i];
        }
        entropy += sum * Math.log(sum + Double.MIN_VALUE) / Math.log(2);
        entropy /= sum;
        return entropy;
    }

    // 给一个样本数组及样本的算术和，计算它的熵
    public double getEntropy(int[] arr, int sum) {
        double entropy = 0.0;
        for (int i = 0; i < arr.length; i++) {
            entropy -= arr[i] * Math.log(arr[i] + Double.MIN_VALUE) / Math.log(2);
        }
        entropy += sum * Math.log(sum + Double.MIN_VALUE) / Math.log(2);
        entropy /= sum;
        return entropy;
    }

    public boolean infoPure(List<Integer> subset) {
        Object value = data.get(subset.get(0)).get(decatt);
        for (int i = 1; i < subset.size(); i++) {
            Object next = data.get(subset.get(i)).get(decatt);
            // equals表示对象内容相同，==表示两个对象指向的是同一片内存
            if (!value.equals(next))
                return false;
        }
        return true;
    }

    // 给定原始数据的子集(subset中存储行号),当以第index个属性为节点时计算它的信息熵
    public double calNodeEntropy(List<Integer> subset, String attr) {

        int sum = subset.size();
        double entropy = 0.0;
        int[][] info = new int[attributevalue.get(attr).size()][];
        for (int i = 0; i < info.length; i++)
            info[i] = new int[attributevalue.get(decatt).size()];

        int[] count = new int[attributevalue.get(attr).size()];
        for (int i = 0; i < sum; i++) {
            int n = subset.get(i);
            Object nodevalue = data.get(n).get(attr);
            int nodeind = attributevalue.get(attr).indexOf(nodevalue);
            count[nodeind]++;
            Object decvalue = data.get(n).get(decatt);
            int decind = attributevalue.get(decatt).indexOf(decvalue);
            info[nodeind][decind]++;
        }

        for (int i = 0; i < info.length; i++) {
            entropy += getEntropy(info[i]) * count[i] / sum;
        }
        return entropy;
    }

    // 构建决策树
    public void buildDT(Tree a, ArrayList<Integer> subset, LinkedList<String> selatt) {

        // Element ele = null;
        // @SuppressWarnings("unchecked")
        // List<Element> list = root.selectNodes("//" + name);
        // Iterator<Element> iter = list.iterator();
        // while (iter.hasNext()) {
        // ele = iter.next();
        // if (ele.attributeValue("value").equals(value))
        // break;
        // }
        // if (infoPure(subset)) {
        // ele.setText(data.get(subset.get(0)).get(decatt));
        // return;
        // }
        String nodeName = null;// attribute.get(minIndex);
        // String minIndex = null;

        double minEntropy = Double.MAX_VALUE;
        for (int i = 0; i < selatt.size(); i++) {
            if (decatt.equals(selatt.get(i)))
                continue;

            double entropy = calNodeEntropy(subset, selatt.get(i));
            if (entropy < minEntropy) {
                nodeName = selatt.get(i);
                minEntropy = entropy;
            }
        }

        // String nodeName = attribute.get(minIndex);
        selatt.remove(nodeName);
        List<Object> attvalues = attributevalue.get(nodeName);
        for (Object val : attvalues) {
            // ele.addElement(nodeName).addAttribute("value", val);
            ArrayList<Integer> al = new ArrayList<Integer>();
            for (int i = 0; i < subset.size(); i++) {
                if (X.isSame(data.get(subset.get(i)).get(nodeName), val)) {
                    al.add(subset.get(i));
                }
            }

            Attribute a1 = a.get(nodeName).set("name", val).set("precent", al.size() * 100f / subset.size());

            buildDT(a, al, selatt);
        }
    }
}
