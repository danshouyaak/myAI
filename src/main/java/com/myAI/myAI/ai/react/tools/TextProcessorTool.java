package com.myAI.myAI.ai.react.tools;

import com.myAI.myAI.ai.react.ReactTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文本处理工具
 * 提供各种文本分析和处理功能
 */
@Component
@Slf4j
public class TextProcessorTool implements ReactTool {
    
    @Override
    public String getName() {
        return "TEXT_PROCESSOR";
    }
    
    @Override
    public String getDescription() {
        return "文本处理工具，支持文本分析、统计、格式化、提取等操作";
    }
    
    @Override
    public String execute(String input) {
        try {
            if (input == null || input.trim().isEmpty()) {
                return "错误：请提供要处理的文本和操作类型";
            }
            
            // 解析输入格式：操作类型|文本内容
            String[] parts = input.split("\\|", 2);
            if (parts.length < 2) {
                return "错误：请按照格式提供输入：操作类型|文本内容";
            }
            
            String operation = parts[0].trim().toLowerCase();
            String text = parts[1].trim();
            
            log.info("执行文本处理操作: {}", operation);
            
            switch (operation) {
                case "count":
                case "统计":
                    return countText(text);
                case "extract_emails":
                case "提取邮箱":
                    return extractEmails(text);
                case "extract_urls":
                case "提取链接":
                    return extractUrls(text);
                case "extract_numbers":
                case "提取数字":
                    return extractNumbers(text);
                case "word_frequency":
                case "词频统计":
                    return wordFrequency(text);
                case "format":
                case "格式化":
                    return formatText(text);
                case "clean":
                case "清理":
                    return cleanText(text);
                case "summary":
                case "摘要":
                    return summarizeText(text);
                default:
                    return "错误：不支持的操作类型。支持的操作：count, extract_emails, extract_urls, extract_numbers, word_frequency, format, clean, summary";
            }
            
        } catch (Exception e) {
            log.error("文本处理失败", e);
            return "错误：文本处理失败 - " + e.getMessage();
        }
    }
    
    @Override
    public String getParameterDescription() {
        return "格式：操作类型|文本内容\n" +
               "支持的操作类型：\n" +
               "- count/统计: 统计字符数、单词数、行数\n" +
               "- extract_emails/提取邮箱: 提取文本中的邮箱地址\n" +
               "- extract_urls/提取链接: 提取文本中的URL链接\n" +
               "- extract_numbers/提取数字: 提取文本中的数字\n" +
               "- word_frequency/词频统计: 统计单词出现频率\n" +
               "- format/格式化: 格式化文本（去除多余空格等）\n" +
               "- clean/清理: 清理文本（去除特殊字符）\n" +
               "- summary/摘要: 生成文本摘要\n" +
               "示例: 'count|这是一段测试文本'";
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
    
    /**
     * 统计文本信息
     */
    private String countText(String text) {
        int charCount = text.length();
        int charCountNoSpaces = text.replaceAll("\\s", "").length();
        int wordCount = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
        int lineCount = text.split("\n").length;
        int paragraphCount = text.split("\n\\s*\n").length;
        
        return String.format(
            "文本统计结果:\n" +
            "- 总字符数: %d\n" +
            "- 字符数(不含空格): %d\n" +
            "- 单词数: %d\n" +
            "- 行数: %d\n" +
            "- 段落数: %d",
            charCount, charCountNoSpaces, wordCount, lineCount, paragraphCount
        );
    }
    
    /**
     * 提取邮箱地址
     */
    private String extractEmails(String text) {
        Pattern emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        Matcher matcher = emailPattern.matcher(text);
        
        Set<String> emails = new HashSet<>();
        while (matcher.find()) {
            emails.add(matcher.group());
        }
        
        if (emails.isEmpty()) {
            return "未找到邮箱地址";
        }
        
        return "找到的邮箱地址:\n" + String.join("\n", emails);
    }
    
    /**
     * 提取URL链接
     */
    private String extractUrls(String text) {
        Pattern urlPattern = Pattern.compile("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");
        Matcher matcher = urlPattern.matcher(text);
        
        Set<String> urls = new HashSet<>();
        while (matcher.find()) {
            urls.add(matcher.group());
        }
        
        if (urls.isEmpty()) {
            return "未找到URL链接";
        }
        
        return "找到的URL链接:\n" + String.join("\n", urls);
    }
    
    /**
     * 提取数字
     */
    private String extractNumbers(String text) {
        Pattern numberPattern = Pattern.compile("-?\\d+(?:\\.\\d+)?");
        Matcher matcher = numberPattern.matcher(text);
        
        List<String> numbers = new ArrayList<>();
        while (matcher.find()) {
            numbers.add(matcher.group());
        }
        
        if (numbers.isEmpty()) {
            return "未找到数字";
        }
        
        return "找到的数字:\n" + String.join(", ", numbers);
    }
    
    /**
     * 词频统计
     */
    private String wordFrequency(String text) {
        String[] words = text.toLowerCase()
                .replaceAll("[^\\w\\s]", "")
                .split("\\s+");
        
        Map<String, Integer> frequency = new HashMap<>();
        for (String word : words) {
            if (!word.trim().isEmpty()) {
                frequency.put(word, frequency.getOrDefault(word, 0) + 1);
            }
        }
        
        if (frequency.isEmpty()) {
            return "未找到有效单词";
        }
        
        // 按频率排序，取前10个
        List<Map.Entry<String, Integer>> sortedEntries = frequency.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        
        StringBuilder result = new StringBuilder("词频统计结果(前10个):\n");
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            result.append(String.format("- %s: %d次\n", entry.getKey(), entry.getValue()));
        }
        
        return result.toString();
    }
    
    /**
     * 格式化文本
     */
    private String formatText(String text) {
        // 去除多余的空格和换行
        String formatted = text.replaceAll("\\s+", " ")
                .replaceAll("\\n\\s*\\n", "\n\n")
                .trim();
        
        return "格式化后的文本:\n" + formatted;
    }
    
    /**
     * 清理文本
     */
    private String cleanText(String text) {
        // 去除特殊字符，保留字母、数字、基本标点和空格
        String cleaned = text.replaceAll("[^\\w\\s\\u4e00-\\u9fa5.,!?;:()\\[\\]{}\"'-]", "")
                .replaceAll("\\s+", " ")
                .trim();
        
        return "清理后的文本:\n" + cleaned;
    }
    
    /**
     * 生成文本摘要
     */
    private String summarizeText(String text) {
        String[] sentences = text.split("[.!?]+");
        
        if (sentences.length <= 3) {
            return "文本摘要:\n" + text;
        }
        
        // 简单的摘要算法：取前两句和最后一句
        StringBuilder summary = new StringBuilder("文本摘要:\n");
        summary.append(sentences[0].trim()).append("。");
        if (sentences.length > 1) {
            summary.append(sentences[1].trim()).append("。");
        }
        if (sentences.length > 2) {
            summary.append("...").append(sentences[sentences.length - 1].trim()).append("。");
        }
        
        return summary.toString();
    }
}
