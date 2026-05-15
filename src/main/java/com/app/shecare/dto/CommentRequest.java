package com.app.shecare.dto;

import lombok.Data;

@Data
public class CommentRequest {

    private String content;

    private boolean anonymous;

}