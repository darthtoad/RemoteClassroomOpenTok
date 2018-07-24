package com.epicodus.samuelgespass.remoteclassroomopentok.models;

import java.util.Arrays;

/**
 * Created by samuelgespass on 7/23/18.
 */

public class WordList {
    private String title;
    private String[] words;
    private String[] imageUrls;

    public WordList(String title, String[] words, String[] imageUrls) {
        this.title = title;
        this.words = words;
        this.imageUrls = imageUrls;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getWords() {
        return words;
    }

    public void setWords(String[] words) {
        this.words = words;
    }

    public String[] getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String[] imageUrls) {
        this.imageUrls = imageUrls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WordList wordList = (WordList) o;

        if (!title.equals(wordList.title)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(words, wordList.words)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(imageUrls, wordList.imageUrls);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + Arrays.hashCode(words);
        result = 31 * result + Arrays.hashCode(imageUrls);
        return result;
    }
}
