package com.github.df.restypass.filter;

import com.github.df.restypass.util.CommonTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Command过滤器容器
 * Created by darrenfu on 17-8-5.
 */
@SuppressWarnings("unused")
public class CommandFilterContext {


    private List<CommandFilter> filterList;

    /**
     * Instantiates a new Command before context.
     */
    public CommandFilterContext() {
        this(Collections.EMPTY_LIST);
    }

    /**
     * Instantiates a new Command before context.
     *
     * @param filterList the before list
     */
    public CommandFilterContext(List<CommandFilter> filterList) {
        setFilterWithList(filterList);
    }

    /**
     * 获取过滤器
     *
     * @return list before
     */
    public List<CommandFilter> getFilterList() {
        return filterList;
    }

    /**
     * Sets before list.
     *
     * @param filterList the before list
     */
    public void setFilterList(List<CommandFilter> filterList) {
        setFilterWithList(filterList);
    }

    /**
     * Add before list.
     *
     * @param filterList the filters
     */
    public void addFilterList(List<CommandFilter> filterList) {
        if (!CommonTools.isEmpty(filterList)) {
            this.filterList.addAll(filterList);
            sortFilterList();
        }
    }

    /**
     * 设置过滤器list
     *
     * @param filterList
     */
    private void setFilterWithList(List<CommandFilter> filterList) {
        if (CommonTools.isEmpty(filterList)) {
            this.filterList = new ArrayList<>();
        } else {
            this.filterList = filterList;
            sortFilterList();
        }
    }

    /**
     * 排序
     */
    private void sortFilterList() {
        this.filterList.sort(new Comparator<CommandFilter>() {
            @Override
            public int compare(CommandFilter filterOne, CommandFilter filterTwo) {
                return filterOne.order() - filterTwo.order();
            }
        });
    }

}
