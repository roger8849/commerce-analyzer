package co.edu.unal.migration.commerceanalyzer.dto;

import javax.validation.constraints.NotNull;

public class WordCrawlerInputParams {
    @NotNull
    private String database;
    @NotNull
    private Boolean shouldReplaceText;

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public Boolean getShouldReplaceText() {
        return shouldReplaceText;
    }

    public void setShouldReplaceText(Boolean shouldReplaceText) {
        this.shouldReplaceText = shouldReplaceText;
    }
}
