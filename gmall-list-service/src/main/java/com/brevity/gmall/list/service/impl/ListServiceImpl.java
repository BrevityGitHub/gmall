package com.brevity.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.brevity.gmall.bean.SkuLsInfo;
import com.brevity.gmall.bean.SkuLsParams;
import com.brevity.gmall.bean.SkuLsResult;
import com.brevity.gmall.config.RedisUtil;
import com.brevity.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    // 把商品信息保存到ES中，不需要注入mapper去操作数据库
    @Autowired
    private JestClient jestClient;
    @Autowired
    private RedisUtil redisUtil;
    public static final String ES_INDEX = "gmall";
    public static final String ES_TYPE = "skuInfo";

    /**
     * 从数据库中查询并赋值给skuLsInfo实体类
     *
     * @param skuLsInfo
     */
    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {
        // 定义执行的动作  PUT /index/type/id {skuLsInfo}
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();
        // 执行动作
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {
        /*
         1. 定义DSL语句
         2. 定义执行的动作
         3. 得到结果集并返回
         */
        String query = makeQueryStringForSearch(skuLsParams);
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult = null;

        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkuLsResult skuLsResult = makeResultForSearch(skuLsParams, searchResult);
        return skuLsResult;
    }

    // 数据结果集转化
    public SkuLsResult makeResultForSearch(SkuLsParams skuLsParams, SearchResult searchResult) {
        SkuLsResult skuLsResult = new SkuLsResult();

        // 用一个集合存储skuLsInfo对象
        ArrayList<SkuLsInfo> arrayList = new ArrayList<>();

        // 通过DSL语句查询的数据存放在searchResult对象中
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        if (hits != null && hits.size() > 0) {
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo skuLsInfo = hit.source;
                // skuLsInfo中的skuName并不是高亮，设置高亮
                // 取highlight对象
                if (hit.highlight != null && hit.highlight.size() > 0) {
                    List<String> list = hit.highlight.get("skuName");
                    // 获取集合中的skuName
                    String skuNameHighlight = list.get(0);
                    skuLsInfo.setSkuName(skuNameHighlight);
                }
                arrayList.add(skuLsInfo);
            }
        }

        skuLsResult.setSkuLsInfoList(arrayList);

        skuLsResult.setTotal(searchResult.getTotal());

        Long totalPages = (searchResult.getTotal() + skuLsParams.getPageSize() - 1) / skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPages);

        ArrayList<String> stringArrayList = new ArrayList<>();
        // 通过集合来获取平台属性值id
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_att = aggregations.getTermsAggregation("groupby_att");
        List<TermsAggregation.Entry> buckets = groupby_att.getBuckets();
        if (buckets != null && buckets.size() > 0) {
            for (TermsAggregation.Entry bucket : buckets) {
                String valueId = bucket.getKey();
                stringArrayList.add(valueId);
            }
        }
        skuLsResult.setAttrValueIdList(stringArrayList);
        return skuLsResult;
    }

    // 根据用户输入的条件动态生成DSL语句
    public String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        // 定义一个查询器 = { }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // query-bool = QueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 通过keyword检索
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {
            // bool-must-match "skuName":"小米手机"
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);
            // 设置高亮
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();
            // highlighter给其赋值，设置高亮的前缀和后缀
            highlighter.preTags("<span style=color:red>");
            highlighter.field("skuName");
            highlighter.postTags("</span>");
            // 将高亮对象放入查询器
            searchSourceBuilder.highlight(highlighter);
        }

        // 判断是否有三级分类id
        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0) {
            // {"term":{"catalog3Id":"61"}}
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            // bool-filter-term
            boolQueryBuilder.filter(termQueryBuilder);
        }

        // 判断平台属性值id
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            // {"term":{"skuAttrValueList.valueId":"82"}}
            for (String valueId : skuLsParams.getValueId()) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                // bool-filter-term
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        // 定义query
        searchSourceBuilder.query(boolQueryBuilder);

        // 设置分页
        // 分页的起始位置
        int from = (skuLsParams.getPageNo() - 1) * skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());

        // 设置排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        // 设置聚合
        TermsBuilder groupby_att = AggregationBuilders.terms("groupby_att").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_att);

        // 将查询器searchSourceBuilder转化为字符串
        String query = searchSourceBuilder.toString();
        System.out.println(query);
        return query;
    }

    @Override
    public void updateHotScore(String skuId) {
        Jedis jedis = redisUtil.getJedis();
        // 定义redis中的key
        String hotScoreKey = "hotScore";
        Double hotScore = jedis.zincrby(hotScoreKey, 1, "skuId:" + skuId);
        // 设置redis中更新10次后放入ES
        if (hotScore % 10 == 0) {
            // 更新一次ES  round：四舍五入
            updateES(skuId, Math.round(hotScore));
        }
    }

    // 更新ES
    public void updateES(String skuId, long hotScore) {
        // 定义DSL语句
        String updateQuery = "{\n" +
                "\t\"doc\":{\n" +
                "\t\t\"hotScore\":" + hotScore + "\n" +
                "\t}\n" +
                "}";
        // 定义动作
        Update update = new Update.Builder(updateQuery).index(ES_INDEX).type(ES_TYPE).id(skuId).build();

        try {
            // 执行
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
