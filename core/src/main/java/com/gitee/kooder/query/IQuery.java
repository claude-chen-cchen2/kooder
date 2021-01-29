package com.gitee.kooder.query;

import com.gitee.kooder.models.QueryResult;
import com.gitee.kooder.models.Searchable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 查询接口
 * @author Winter Lau<javayou@gmail.com>
 */
public interface IQuery {

    /**
     * 索引类型
     * @return
     */
    public String type();

    /**
     * 搜索关键字
     * @param key
     */
    IQuery setSearchKey(String key);

    /**
     * 是否解析查询字符串
     * @param parseSearchKey
     * @return
     */
    IQuery setParseSearchKey(boolean parseSearchKey);

    /**
     * 排序方法
     * @param sort
     * @return
     */
    IQuery setSort(String sort);

    /**
     * 页码
     * @param page
     * @return
     */
    IQuery setPage(int page);

    /**
     * 页大小
     * @param pageSize
     * @return
     */
    IQuery setPageSize(int pageSize);

    /**
     * 添加扩展属性
     * @param name
     * @param value
     * @return
     */
    IQuery addFacets(String name, String value);

    /**
     * 获取扩展属性
     * @return
     */
    Map<String, String[]> getFacets();

    /**
     * 添加过滤条件
     * @param filterQueryString
     * @return
     */
    IQuery addFilter(String filterQueryString);

    /**
     * 获取所有过滤条件
     * @return
     */
    List<String> getFilters();

    /**
     * execute query
     * @return
     * @throws IOException
     */
    QueryResult execute() throws IOException;

    /**
     * Get max object indexed
     * @return
     */
    public Searchable getLastestObject() ;
}