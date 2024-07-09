package com.gec.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gec.common.DateString;
import com.gec.entity.Analysis;
import com.gec.entity.Module;
import com.gec.entity.Tc;
import com.gec.entity.TcStandard;
import com.gec.enums.ApprovalStatus;
import com.gec.enums.EnabledStatus;
import com.gec.service.TcStandardService;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;

import java.util.*;

import static com.gec.enums.ApprovalStatus.Approval;
import static com.gec.enums.ApprovalStatus.DisApproval;
import static com.gec.enums.EnabledStatus.Enabled;

public class TcTextComparatorUtils {

    //用例分析中的计算
    public static void AnalysisSimilarComputed(Tc tc, Analysis analysis, Module mod, TcStandardService tcStandardService) {
        //根据最相似的标题查找标准
        QueryWrapper<TcStandard> standardQueryWrapper = new QueryWrapper();
        standardQueryWrapper.eq("mod_code", tc.getModCode());
        standardQueryWrapper.eq("is_delete", "0");
        List<TcStandard> standardList = tcStandardService.list(standardQueryWrapper);
        List<Double> titleSimilarList = new ArrayList<>();
        for (int i = 0; i < standardList.size(); i++) {
            //用例的标题和所有的标题都计算一次
            titleSimilarList.add(TcTextComparatorUtils.getCosineSimilarity(standardList.get(i).getStadTitle(), tc.getTcTitle()));
        }
        Double max = Collections.max(titleSimilarList);
        //最符合的标准
        TcStandard tcStandard = standardList.get(titleSimilarList.indexOf(max));
        analysis.setStadCode(tcStandard.getStadCode());
        analysis.setModCode(mod.getModCode());
        //设置分析中跟用例一样的值
        analysis.setTcTitle(tc.getTcTitle());
        analysis.setTcDesc(tc.getTcDesc());
        analysis.setTcCondition(tc.getTcCondition());
        analysis.setInput(tc.getInput());
        analysis.setStep(tc.getStep());
        analysis.setResult(tc.getResult());
        analysis.setImportance(tc.getImportance());
        //和标准进行相似度计算
        //描述相似度
        Double descSimilar = TcTextComparatorUtils.getCosineSimilarity(tcStandard.getStadDesc(), tc.getTcDesc());
        //结果相似度
        Double resultSimilar = TcTextComparatorUtils.getCosineSimilarity(tcStandard.getResult(), tc.getResult());
        analysis.setDescSimi(String.valueOf(descSimilar * 100));
        analysis.setResultSimi(String.valueOf(resultSimilar * 100));

        //单项成绩
        analysis.setCompScore(String.valueOf(descSimilar + resultSimilar));
        analysis.setUpdateDate(DateUtils.getCurrentData(DateString.yyMMdd));
        analysis.setOperator(tc.getOperator());
        analysis.setIsApproval(mod.getIsApproval().equals(Approval) ? DisApproval : Approval);
        analysis.setIsDelete(Enabled);
    }

    //用例分析中的计算
    public static void AnalysisSimilarComputed(Analysis analysis, Module mod, TcStandardService tcStandardService) {
        //根据最相似的标题查找标准
        QueryWrapper<TcStandard> standardQueryWrapper = new QueryWrapper();
        standardQueryWrapper.eq("mod_code", analysis.getModCode());
        standardQueryWrapper.eq("is_delete", "0");
        List<TcStandard> standardList = tcStandardService.list(standardQueryWrapper);
        List<Double> titleSimilarList = new ArrayList<>();
        for (int i = 0; i < standardList.size(); i++) {
            //用例的标题和所有的标题都计算一次
            titleSimilarList.add(TcTextComparatorUtils.getCosineSimilarity(standardList.get(i).getStadTitle(), analysis.getTcTitle()));
        }
        Double max = Collections.max(titleSimilarList);
        //最符合的标准
        TcStandard tcStandard = standardList.get(titleSimilarList.indexOf(max));
        analysis.setStadCode(tcStandard.getStadCode());
        //描述相似度
        Double descSimilar = TcTextComparatorUtils.getCosineSimilarity(tcStandard.getStadDesc(), analysis.getTcDesc());
        //结果相似度
        Double resultSimilar = TcTextComparatorUtils.getCosineSimilarity(tcStandard.getResult(), analysis.getResult());
        //NAN说明相似度为0
        analysis.setDescSimi(String.valueOf(descSimilar * 100));
        analysis.setResultSimi(String.valueOf(resultSimilar * 100));
        //单项成绩
        analysis.setCompScore(String.valueOf(descSimilar + resultSimilar));
        analysis.setUpdateDate(DateUtils.getCurrentData(DateString.yyMMdd));
        analysis.setOperator(analysis.getOperator());
        analysis.setIsApproval(mod.getIsApproval().equals(Approval) ? DisApproval : Approval);
        analysis.setIsDelete(Enabled);
    }

    /**
     * @author Llunch4w
     * @create 2021-04-02 17:52
     */
    // 两两对比函数
    public static Double getCosineSimilarity(String textA, String textB) {
        // 从文本中提取出关键词数组
        List<String> wordListA = TcTextComparatorUtils.extractWordFromText(textA);
        List<String> wordListB = TcTextComparatorUtils.extractWordFromText(textB);

        List<Double> vectorA = new ArrayList<>();
        List<Double> vectorB = new ArrayList<>();
        // 将关键词数组转换为词向量并保存在 vectorA 和 vectorB 中
        TcTextComparatorUtils.convertWordList2Vector(wordListA, wordListB, vectorA, vectorB);

        // 计算向量夹角的余弦值
        double cosine = Double.parseDouble(String.format("%.4f", TcTextComparatorUtils.countCosine(vectorA, vectorB)));
        //如果算不下去了那就肯定是 0÷0 那么肯定是没有相似的
        if (String.valueOf(cosine).equals("NaN")) {
            return 0.0;
        }
        return cosine;
    }

    // 提取文本中有实意的词
    private static List<String> extractWordFromText(String text) {
        // resultList 用于保存提取后的结果
        List<String> resultList = new ArrayList<>();

        // 当 text 为空字符串时，使用分词函数会报错，所以需要提前处理这种情况
        if (text.length() == 0) {
            return resultList;
        }

        // 分词
        List<Term> termList = HanLP.segment(text);
        // 提取所有的 1.名词/n ; 2.动词/v ; 3.形容词/a
        for (Term term : termList) {
            if (term.nature == Nature.n || term.nature == Nature.v || term.nature == Nature.a
                    || term.nature == Nature.vn || term.nature == Nature.m) {
                resultList.add(term.word);
            }
        }

        return resultList;
    }

    /**
     * @param wordListA : 文本 A 的单词数组
     * @param wordListB : 文本 B 的单词数组
     * @param vectorA   : 文本 A 转换成为的向量 A
     * @param vectorB   : 文本 B 转换成为的向量 B
     * @return vocabulary : 词汇表
     * @Description : 将单词数组转换为单词向量，结果保存在 vectorA 和 vectorB 里
     */
    private static List<String> convertWordList2Vector(List<String> wordListA, List<String> wordListB, List<Double> vectorA, List<Double> vectorB) {
        // 词汇表
        List<String> vocabulary = new ArrayList<>();

        // 获取词汇表 wordListA 的频率表，并同时建立词汇表
        Map<String, Double> frequencyTableA = buildFrequencyTable(wordListA, vocabulary);

        // 获取词汇表 wordListB 的频率表，并同时建立词汇表
        Map<String, Double> frequencyTableB = buildFrequencyTable(wordListB, vocabulary);

        // 根据频率表得到向量
        getWordVectorFromFrequencyTable(frequencyTableA, vectorA, vocabulary);
        getWordVectorFromFrequencyTable(frequencyTableB, vectorB, vocabulary);

        return vocabulary;
    }

    /**
     * @param wordList：单词数组
     * @param vocabulary:   词汇表
     * @return Map<String, Double>: key为单词，value为频率
     * @Description 建立词汇表 wordList 的频率表，并同时建立词汇表
     */
    private static Map<String, Double> buildFrequencyTable(List<String> wordList, List<String> vocabulary) {
        // 先建立频数表
        Map<String, Integer> countTable = new HashMap<>();
        for (String word : wordList) {
            if (countTable.containsKey(word)) {
                countTable.put(word, countTable.get(word) + 1);
            } else {
                countTable.put(word, 1);
            }
            // 词汇表中是无重复元素的，所以只在 vocabulary 中没有该元素时才加入
            if (!vocabulary.contains(word)) {
                vocabulary.add(word);
            }
        }
        // totalCount 用于记录词出现的总次数
        int totalCount = wordList.size();
        // 将频数表转换为频率表
        Map<String, Double> frequencyTable = new HashMap<>();
        for (String key : countTable.keySet()) {
            frequencyTable.put(key, (double) countTable.get(key) / totalCount);
        }
        return frequencyTable;
    }

    /**
     * @param frequencyTable : 频率表
     * @param wordVector     : 转换后的词向量
     * @param vocabulary     : 词汇表
     * @Description 根据词汇表和文本的频率表计算词向量，最后 wordVector 和 vocabulary 应该是同维的
     */
    private static void getWordVectorFromFrequencyTable(Map<String, Double> frequencyTable, List<Double> wordVector, List<String> vocabulary) {
        for (String word : vocabulary) {
            double value = 0.0;
            if (frequencyTable.containsKey(word)) {
                value = frequencyTable.get(word);
            }
            wordVector.add(value);
        }
    }

    /**
     * @param vectorA : 词向量 A
     * @param vectorB : 词向量 B
     * @return
     * @Description 计算向量 A 和向量 B 的夹角余弦值
     */
    private static double countCosine(List<Double> vectorA, List<Double> vectorB) {
        // 分别计算向量的平方和
        double sqrtA = countSquareSum(vectorA);
        double sqrtB = countSquareSum(vectorB);

        // 计算向量的点积
        double dotProductResult = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            dotProductResult += vectorA.get(i) * vectorB.get(i);
        }

        return dotProductResult / (sqrtA * sqrtB);
    }

    // 计算向量平方和的开方
    private static double countSquareSum(List<Double> vector) {
        double result = 0.0;
        for (Double value : vector) {
            result += value * value;
        }
        return Math.sqrt(result);
    }

}
