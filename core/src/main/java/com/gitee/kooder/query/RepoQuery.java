/**
 * Copyright (c) 2021, OSChina (oschina.net@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitee.kooder.query;

import com.gitee.kooder.core.AnalyzerFactory;
import com.gitee.kooder.core.Constants;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 仓库搜索
 * @author Winter Lau<javayou@gmail.com>
 */
public class RepoQuery extends QueryBase {

    public final static int SCORE_FACTOR = 6;

    private HashMap<String, Method> scoreMethods;
    private static Expression repoScoreExpr;

    public RepoQuery() {
        scoreMethods = new HashMap();
        try {
            scoreMethods.putAll(JavascriptCompiler.DEFAULT_FUNCTIONS);
            scoreMethods.put("gsort", getClass().getDeclaredMethod("sort", double.class, double.class, double.class, double.class));
            repoScoreExpr = JavascriptCompiler.compile("gsort($score,$recomm,$stars,$gindex)", scoreMethods, getClass().getClassLoader());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String type() {
        return Constants.TYPE_REPOSITORY;
    }

    /**
     * list facet names
     * @return
     */
    @Override
    protected List<String> listFacetFields() {
        return Arrays.asList(Constants.FIELD_LANGUAGE, Constants.FIELD_LICENSE);
    }

    @Override
    protected Query buildQuery() {
        Query query = super.buildQuery();
        return new FunctionScoreQuery(query, getRepoScoreExpression());
    }

    /**
     * 构建查询对象
     *
     * @return
     */
    @Override
    protected Query buildUserQuery() {
        if(parseSearchKey) {
            QueryParser parser = new QueryParser("repo", AnalyzerFactory.getInstance(false));
            try {
                return parser.parse(searchKey);
            } catch (ParseException e) {
                throw new QueryException("Failed to parse \""+searchKey+"\"", e);
            }
        }
        String q = QueryParser.escape(searchKey);

        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        //只搜索公开仓库
        //builder.add(IntPoint.newExactQuery(Constants.FIELD_VISIBILITY, Constants.VISIBILITY_PUBLIC), BooleanClause.Occur.FILTER);
        //不搜索fork仓库
        builder.add(LongPoint.newExactQuery(Constants.FIELD_FORK, Constants.REPO_FORK_NO), BooleanClause.Occur.FILTER);
        //不搜索被屏蔽的仓库
        builder.add(IntPoint.newExactQuery(Constants.FIELD_BLOCK, Constants.REPO_BLOCK_NO), BooleanClause.Occur.FILTER);

        //BoostQuery
        //如果调整  boost 就要调整 ScoreHelper 中的 SCORE_FACTOR
        BooleanQuery.Builder qbuilder = new BooleanQuery.Builder();
        qbuilder.add(makeBoostQuery(Constants.FIELD_CATALOGS, q, 10.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery(Constants.FIELD_NAME, q, SCORE_FACTOR), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery(Constants.FIELD_DESC, q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery(Constants.FIELD_README, q, 0.5f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery(Constants.FIELD_TAGS, q, 1.0f), BooleanClause.Occur.SHOULD);
        //qbuilder.add(makeBoostQuery("lang", q, 2.0f), BooleanClause.Occur.SHOULD);
        qbuilder.add(makeBoostQuery(Constants.FIELD_USER_NAME, q, 1.0f), BooleanClause.Occur.SHOULD);
        qbuilder.setMinimumNumberShouldMatch(1);

        builder.add(qbuilder.build(), BooleanClause.Occur.MUST);

        return builder.build();
    }

    /**
     * 对搜索加权
     * @param field
     * @param q
     * @param boost
     * @return
     */
    @Override
    protected BoostQuery makeBoostQuery(String field, String q, float boost) {
        /*
        if("name".equals(field)){
            List<String> keys = SearchHelper.splitKeywords(q);
            BooleanQuery.Builder qbuilder = new BooleanQuery.Builder();
            for(int i=0;i<keys.size();i++) {
                String key = keys.get(i);
                qbuilder.add(new WildcardQuery(new Term(field, "*"+key+"*")), BooleanClause.Occur.SHOULD);
            }
            Query query = qbuilder.build();
            return new BoostQuery(query, boost);
        }
        */
        return super.makeBoostQuery(field, q, boost);
    }

    /**
     * custom query score
     * @return
     */
    private DoubleValuesSource getRepoScoreExpression() {
        SimpleBindings bindings = new SimpleBindings();
        bindings.add("$score", DoubleValuesSource.SCORES);
        bindings.add("$recomm", DoubleValuesSource.fromIntField(Constants.FIELD_RECOMM));
        bindings.add("$stars", DoubleValuesSource.fromIntField(Constants.FIELD_STAR_COUNT));
        bindings.add("$gindex", DoubleValuesSource.fromIntField(Constants.FIELD_G_INDEX));
        return repoScoreExpr.getDoubleValuesSource(bindings);
    }

    /**
     * 构建排序对象
     * @return
     */
    @Override
    protected Sort buildSort() {
        if("stars".equals(sort))
            return new Sort(new SortedNumericSortField(Constants.FIELD_STAR_COUNT, SortField.Type.LONG, true));
        if("forks".equals(sort))
            return new Sort(new SortedNumericSortField(Constants.FIELD_FORK_COUNT, SortField.Type.LONG, true));
        if("update".equals(sort))
            return new Sort(new SortedNumericSortField(Constants.FIELD_UPDATED_AT, SortField.Type.LONG, true));
        return Sort.RELEVANCE;
    }

    /**
     * 自定义仓库的搜索评分规则（该方法提供给 Lucene 调用，所以必须是 public static 方法）
     * @param score
     * @param recomm
     * @param stars
     * @param gindex
     * @return
     */
    public static double sort(double score, double recomm, double stars, double gindex) {
        if(score >= SCORE_FACTOR) {
            //官方推荐加权
            if(recomm > 0)
                score += (recomm * 20);
            else {
                //Star 数加权
                if (stars >= 100)
                    score += stars / 20;
                else if (stars > 0 && stars < 10)
                    score -= 10;
                else
                    score -= 20;
            }
            while(score < SCORE_FACTOR)
                score += 1;
        }
        return score;
    }

}
