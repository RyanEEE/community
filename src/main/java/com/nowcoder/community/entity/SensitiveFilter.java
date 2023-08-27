package com.nowcoder.community.entity;

import lombok.Data;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    @Data
    private class TrieNode{
        //关键词结束标识
        private boolean isKeywordEnd = false;
        private Map<Character, TrieNode> trieNodeMap = new HashMap<>();
        public void addTrieNodeMap(Character c, TrieNode trieNode){
            trieNodeMap.put(c, trieNode);
        }
        public TrieNode getTrieNode(Character c){
            return trieNodeMap.get(c);
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    private static final String REPLACEMENT = "***";
    private TrieNode rootNode = new TrieNode();
    @PostConstruct
    public void init(){
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ){
            String keyword;
            while((keyword=reader.readLine())!=null){
                this.addKeyword(keyword);
            }

        }catch (IOException e){
            logger.error("加载敏感词文件失败");
        }

    }

    /**
     * 过滤敏感词
     * @param text
     * @return
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();
        while(position<text.length()){
            char c = text.charAt(position);
            //跳过符号
            if(isSymbol(c)){
                if(tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            //检查下节点
            tempNode = tempNode.getTrieNode(c);
            if(tempNode==null){
                //begin不是敏感词
                sb.append(text.charAt(begin));
                position = ++begin;
                //指针归位
                tempNode = rootNode;
            }else if(tempNode.isKeywordEnd()){
                //发现敏感词
                sb.append(REPLACEMENT);
                begin = ++position;
                tempNode = rootNode;
            }else{
                //检查下一个字符
                position++;
            }
        }
        //最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }
    private boolean isSymbol(Character c){

        return !CharUtils.isAsciiAlphanumeric(c) && (c<0x2E80 || c>0x9FFF);
    }

    private void addKeyword(String keyword){
        TrieNode tempNode = rootNode;
        for(int i=0;i<keyword.length();i++){
            char c = keyword.charAt(i);
            TrieNode trieNode = tempNode.getTrieNode(c);
            if(trieNode == null){
                trieNode = new TrieNode();
                tempNode.addTrieNodeMap(c,trieNode);
            }
            tempNode = trieNode;
            //设置结束标识
            if(i==keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }

        }
    }

}
