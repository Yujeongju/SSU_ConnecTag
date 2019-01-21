package com.hashtoggle.connectag;

public class Post {
    private String card_keyword;
    private String card_link;
    private String card_cnt;

    public Post(String card_keyword, String card_link, String card_cnt) {
        this.card_keyword = card_keyword;
        this.card_link = card_link;
        this.card_cnt = card_cnt;
    }

    public String getCard_keyword() {
        return card_keyword;
    }

    public String getCard_link() {
        return card_link;
    }

    public String getCard_cnt() { return card_cnt; }
}
