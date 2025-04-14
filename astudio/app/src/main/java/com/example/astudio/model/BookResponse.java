package com.example.astudio.model;

import java.util.List;

public class BookResponse {
    public List<Item> items;

    public static class Item {
        public VolumeInfo volumeInfo;
    }

    public static class VolumeInfo {
        public String title;
        public ImageLinks imageLinks;
        public Float averageRating;
        public List<String> authors;
        public String description;
    }

    public static class ImageLinks {
        public String thumbnail;
    }
}