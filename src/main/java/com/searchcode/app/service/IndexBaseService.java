package com.searchcode.app.service;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.SearchCodeLib;

import java.util.Arrays;
import java.util.List;

public abstract class IndexBaseService implements IIndexService {

    protected SearchCodeLib searchcodeLib;

    protected List<String> indexAllFields; // Contains the fields that should be added to the all portion of the index

    public IndexBaseService() {
        this.indexAllFields = Arrays.asList(Properties.getProperties().getProperty(Values.INDEX_ALL_FIELDS, Values.DEFAULT_INDEX_ALL_FIELDS).split(","));
        this.searchcodeLib = Singleton.getSearchCodeLib();
    }

    public IndexBaseService(SearchCodeLib searchCodeLib) {
        this();
        this.searchcodeLib = searchCodeLib;
    }

    public String indexContentPipeline(CodeIndexDocument codeIndexDocument) {
        // This is the main pipeline for making code searchable and probably the most important
        // part of the indexer codebase
        StringBuilder indexBuilder = new StringBuilder();

        if (this.indexAllFields.contains("filename")) {
            indexBuilder.append(this.searchcodeLib.codeCleanPipeline(codeIndexDocument.getFileName())).append(" ");
        }
        if (this.indexAllFields.contains("filenamereverse")) {
            indexBuilder.append(new StringBuilder(codeIndexDocument.getFileName()).reverse().toString()).append(" ");
        }
        if (this.indexAllFields.contains("path")) {
            indexBuilder.append(this.searchcodeLib.splitKeywords(codeIndexDocument.getFileName(), true)).append(" ");
            indexBuilder.append(codeIndexDocument.getFileLocationFilename()).append(" ");
            indexBuilder.append(codeIndexDocument.getFileLocation()).append(" ");
        }
        if (this.indexAllFields.contains("content")) {
            indexBuilder.append(this.searchcodeLib.splitKeywords(codeIndexDocument.getContents(), true)).append(" ");
            indexBuilder.append( this.searchcodeLib.codeCleanPipeline(codeIndexDocument.getContents())).append(" ");
        }
        if (this.indexAllFields.contains("interesting")) {
            indexBuilder.append(this.searchcodeLib.findInterestingKeywords(codeIndexDocument.getContents())).append(" ");
            indexBuilder.append(this.searchcodeLib.findInterestingCharacters(codeIndexDocument.getContents()));
        }

        return indexBuilder.toString();
    }
}