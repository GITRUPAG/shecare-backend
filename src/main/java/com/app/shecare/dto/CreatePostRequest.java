package com.app.shecare.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreatePostRequest {

    private String content;

    private boolean anonymous;

    private String category;

    private List<String> hashtags;

    private List<String> mediaUrls;

    private String mediaType;

}