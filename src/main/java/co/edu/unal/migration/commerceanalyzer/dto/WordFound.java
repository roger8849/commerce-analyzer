package co.edu.unal.migration.commerceanalyzer.dto;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WordFound {
    private String filePath;
    private Map<String, List<Integer>> wordsFound;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Map<String, List<Integer>> getWordsFound() {
        return wordsFound;
    }

    public void setWordsFound(Map<String, List<Integer>> wordsFound) {
        this.wordsFound = wordsFound;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof WordFound)) {
            return false;
        }
        WordFound wordFound = (WordFound) obj;
        return StringUtils.equalsIgnoreCase(this.filePath, wordFound.getFilePath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.filePath);
    }
}
