package com.yixuwang.service;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 可过滤替换中间隔着字符的敏感词
 *
 * Created by yixu on 2018/6/27.
 * */
@Service
public class SensitiveService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveService.class);

    private static final String DEFAULT_REPLACEMENT = "敏感词";    //默认敏感词替换符

    private TrieNode rootNode = new TrieNode(); //敏感词树根节点

    /**
     * TrieNode - 字典树，又称单词查找树或键树。
     * 典型应用是用于统计和排序大量的字符串（但不仅限于字符串），所以经常被搜索引擎系统用于文本词频统计。
     * 优点是：最大限度地减少无谓的字符串比较，查询效率比哈希表高。
     * 核心思想是空间换时间：利用字符串的公共前缀来降低查询时间的开销以达到提高效率的目的。
     * */
    private class TrieNode {

        private boolean end = false;    //是否是关键词终点

        private Map<Character, TrieNode> subNodes = new HashMap<>();    //key下一个字符，value是对应的子节点


        //向指定位置添加节点树
        void addSubNode(Character key, TrieNode node) {
            subNodes.put(key, node);
        }

        //获取下个节点
        TrieNode getSubNode(Character key) {
            return subNodes.get(key);
        }

        boolean isKeywordEnd() {
            return end;
        }

        void setKeywordEnd() {
            this.end = true;
        }

        public int getSubNodeCount() {
            return subNodes.size();
        }
    }



    //过滤文本中的敏感词
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }

        StringBuilder result = new StringBuilder();

        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;

        while (position < text.length()) {
            char c = text.charAt(position);

            // 符号字符直接跳过
            if (isSymbol(c)) {
                if (tempNode == rootNode) {
                    result.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            tempNode = tempNode.getSubNode(c);

            // 当前位置的匹配结束
            if (tempNode == null) {
                // 以begin开始的字符串不存在敏感词
                result.append(text.charAt(begin));
                // 跳到下一个字符开始测试
                position = begin + 1;
                begin = position;
                // 回到树初始节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词， 从begin到position的位置用replacement替换掉
                result.append(DEFAULT_REPLACEMENT);
                // 跳到下一步位置
                position = position + 1;
                begin = position;
                tempNode = rootNode;
            } else {
                ++position;
            }
        }

        result.append(text.substring(begin));

        return result.toString();
    }


    //判断是否是符号
    private boolean isSymbol(char c) {
        int ic = (int) c;
        // 0x2E80-0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9FFF);
    }

    //添加敏感词
    private void addWord(String lineTxt) {
        TrieNode tempNode = rootNode;
        // 循环每个字节
        for (int i = 0; i < lineTxt.length(); ++i) {
            // 过滤字符
            Character c = lineTxt.charAt(i);
            if (isSymbol(c)) {
                continue;
            }

            TrieNode node = tempNode.getSubNode(c);

            if (node == null) { // 没初始化
                node = new TrieNode();
                tempNode.addSubNode(c, node);
            }

            tempNode = node;    //当前 node 指向 子node

            if (i == lineTxt.length() - 1) {
                // 关键词结束， 设置结束标志
                tempNode.setKeywordEnd();
            }
        }
    }

    /**
     * 初始化bean的时候执行，可以针对某个具体的bean进行配置。
     * 必须实现 InitializingBean接口，实现 InitializingBean接口必须实现afterPropertiesSet方法。
     * */
    @Override
    public void afterPropertiesSet() throws Exception {

        rootNode = new TrieNode();

        try {
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("SensitiveWords.txt");
            InputStreamReader read = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                lineTxt = lineTxt.trim();
                addWord(lineTxt);
            }
            read.close();
        } catch (Exception e) {
            logger.error("读取敏感词文件失败" + e.getMessage());
        }
    }

/*
    public static void main(String[] argv) {
        SensitiveService s = new SensitiveService();
        //s.addWord("色情");
        s.addWord("好色");
        System.out.print(s.filter("你好*色**情XX"));
    }
    */
}
